import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef,
  HostListener
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  OrderService,
  Order,
  OrderStatus,
  CreateOrderPayload
} from '../../services/order';
import { ProductService, Product } from '../../services/product';

interface FormOrderItem {
  productId: number | null;
  quantity: number;
}

@Component({
  selector: 'app-orders',
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.html',
  styleUrl: './orders.scss'
})
export class Orders implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly productService = inject(ProductService);
  private readonly cdr = inject(ChangeDetectorRef);

  orders: Order[] = [];
  availableProducts: Product[] = [];
  loading = true;
  error = '';
  successMessage = '';

  // Visualización del detalle de pedido
  selectedOrder: Order | null = null;

  // Formulario de creación
  showForm = false;
  submitting = false;
  formError = '';
  customerName = '';
  orderItems: FormOrderItem[] = [];

  ngOnInit(): void {
    this.loadOrders();
    this.loadProducts();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = '';

    this.orderService.getOrders().subscribe({
      next: (orders: Order[]) => {
        console.log('Pedidos recibidos:', orders);

        this.orders = orders;
        this.loading = false;

        if (this.selectedOrder) {
          const fresh = this.orders.find((o) => o.id === this.selectedOrder!.id);
          if (fresh) {
            this.selectedOrder = fresh;
          }
        }

        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error('Error al cargar pedidos:', error);

        this.error = 'No se pudieron cargar los pedidos.';
        this.loading = false;

        this.cdr.detectChanges();
      }
    });
  }

  loadProducts(): void {
    this.productService.getProducts().subscribe({
      next: (products: Product[]) => {
        this.availableProducts = products;
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error('Error al cargar productos para pedidos:', error);
      }
    });
  }

  toggleForm(): void {
    if (this.showForm) {
      this.closeForm();
    } else {
      this.openForm();
    }
  }

  openForm(): void {
    this.resetForm();
    this.showForm = true;
    this.formError = '';
    this.loadProducts();
  }

  closeForm(): void {
    this.showForm = false;
    this.formError = '';
    this.resetForm();
  }

  resetForm(): void {
    this.customerName = '';
    this.orderItems = [{ productId: null, quantity: 1 }];
  }

  addItem(): void {
    this.orderItems.push({ productId: null, quantity: 1 });
  }

  removeItem(index: number): void {
    this.orderItems.splice(index, 1);
    if (this.orderItems.length === 0) {
      this.addItem();
    }
  }

  getProduct(productId: number | null): Product | undefined {
    if (productId === null) {
      return undefined;
    }
    return this.availableProducts.find((p) => p.id === productId);
  }

  getProductStock(productId: number | null): number {
    const product = this.getProduct(productId);
    return product ? product.stock : 0;
  }

  getItemUnitPrice(item: FormOrderItem): number {
    const product = this.getProduct(item.productId);
    return product ? product.price : 0;
  }

  getItemSubtotal(item: FormOrderItem): number {
    const qty = Number(item.quantity) || 0;
    return this.getItemUnitPrice(item) * qty;
  }

  calculateTotal(): number {
    return this.orderItems.reduce((acc, item) => acc + this.getItemSubtotal(item), 0);
  }

  onProductSelected(item: FormOrderItem): void {
    const product = this.getProduct(item.productId);
    if (product) {
      if (product.stock > 0 && item.quantity > product.stock) {
        item.quantity = product.stock;
      } else if (item.quantity <= 0) {
        item.quantity = 1;
      }
    }
    this.formError = '';
  }

  onQuantityChange(item: FormOrderItem): void {
    const product = this.getProduct(item.productId);
    if (product && item.quantity > product.stock) {
      this.formError = `La cantidad para "${product.name}" no puede exceder el stock disponible (${product.stock}).`;
    } else {
      this.formError = '';
    }
  }

  submitOrder(): void {
    this.formError = '';

    const trimmedCustomer = this.customerName.trim();
    if (!trimmedCustomer) {
      this.formError = 'Ingresa el nombre del cliente.';
      return;
    }

    if (this.orderItems.length === 0) {
      this.formError = 'Debes agregar al menos un producto al pedido.';
      return;
    }

    // Validar cada fila
    for (let i = 0; i < this.orderItems.length; i++) {
      const item = this.orderItems[i];
      if (item.productId === null) {
        this.formError = `Selecciona un producto en la fila #${i + 1}.`;
        return;
      }

      const qty = Number(item.quantity);
      if (!qty || qty <= 0 || !Number.isInteger(qty)) {
        this.formError = `La cantidad en la fila #${i + 1} debe ser un número entero mayor a 0.`;
        return;
      }
    }

    // Validar acumulación por producto frente a stock disponible
    const quantityByProduct = new Map<number, number>();
    for (const item of this.orderItems) {
      if (item.productId !== null) {
        const current = quantityByProduct.get(item.productId) || 0;
        quantityByProduct.set(item.productId, current + Number(item.quantity));
      }
    }

    for (const [productId, totalQty] of quantityByProduct.entries()) {
      const prod = this.getProduct(productId);
      if (!prod) {
        this.formError = 'Producto seleccionado no encontrado en el catálogo.';
        return;
      }

      if (prod.stock <= 0) {
        this.formError = `El producto "${prod.name}" no tiene stock disponible.`;
        return;
      }

      if (totalQty > prod.stock) {
        this.formError = `La cantidad total para "${prod.name}" (${totalQty}) supera el stock disponible (${prod.stock}).`;
        return;
      }
    }

    const total = this.calculateTotal();
    if (total <= 0) {
      this.formError = 'El total del pedido debe ser mayor a 0.';
      return;
    }

    const payload: CreateOrderPayload = {
      customerName: trimmedCustomer,
      total: total,
      items: this.orderItems.map((item) => {
        const prod = this.getProduct(item.productId)!;
        return {
          quantity: Number(item.quantity),
          unitPrice: prod.price,
          product: {
            id: prod.id
          }
        };
      })
    };

    this.submitting = true;

    this.orderService.createOrder(payload).subscribe({
      next: (createdOrder: Order) => {
        this.submitting = false;
        this.showForm = false;
        this.resetForm();
        this.successMessage = `¡Pedido #${createdOrder.id} para ${createdOrder.customerName} creado exitosamente!`;
        this.loadOrders();
        this.loadProducts();

        setTimeout(() => {
          this.successMessage = '';
          this.cdr.detectChanges();
        }, 5000);

        this.cdr.detectChanges();
      },
      error: (error: any) => {
        console.error('Error al crear el pedido:', error);
        this.submitting = false;
        const backendMsg = error?.error?.error || error?.error?.message;
        this.formError = backendMsg || 'No se pudo crear el pedido en el servidor. Revisa los datos e intenta nuevamente.';
        this.cdr.detectChanges();
      }
    });
  }

  updateStatus(order: Order, newStatus: OrderStatus): void {
    this.orderService.updateOrderStatus(order.id, newStatus).subscribe({
      next: (updatedOrder: Order) => {
        const index = this.orders.findIndex(
          (item) => item.id === updatedOrder.id
        );

        if (index !== -1) {
          this.orders[index] = updatedOrder;
        }

        if (this.selectedOrder && this.selectedOrder.id === updatedOrder.id) {
          this.selectedOrder = updatedOrder;
        }

        this.cdr.detectChanges();
      },
      error: (error: any) => {
        console.error('Error al actualizar el estado:', error);
        const backendMsg = error?.error?.error || error?.error?.message;
        this.error = backendMsg || 'No se pudo actualizar el estado del pedido.';
        this.cdr.detectChanges();
      }
    });
  }

  openDetail(order: Order): void {
    this.selectedOrder = order;
  }

  closeDetail(): void {
    this.selectedOrder = null;
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.selectedOrder) {
      this.closeDetail();
    }
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      PENDIENTE: 'Pendiente',
      ACEPTADO: 'Aceptado',
      PREPARANDO: 'Preparando',
      DESPACHADO: 'Despachado',
      CANCELADO: 'Cancelado'
    };

    return labels[status] ?? status;
  }
}

