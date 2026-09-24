import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';

import {
  OrderService,
  Order,
  OrderStatus
} from '../../services/order';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-orders',
  imports: [CommonModule],
  templateUrl: './orders.html',
  styleUrl: './orders.scss'
})
export class Orders implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly cdr = inject(ChangeDetectorRef);

  orders: Order[] = [];
  loading = true;
  error = '';

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = '';

    this.orderService.getOrders().subscribe({
      next: (orders: Order[]) => {
        console.log('Pedidos recibidos:', orders);

        this.orders = orders;
        this.loading = false;

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

  updateStatus(order: Order, newStatus: OrderStatus): void {
    this.orderService.updateOrderStatus(order.id, newStatus).subscribe({
      next: (updatedOrder: Order) => {
        const index = this.orders.findIndex(
          (item) => item.id === updatedOrder.id
        );

        if (index !== -1) {
          this.orders[index] = updatedOrder;
        }

        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        console.error('Error al actualizar el estado:', error);
        this.error = 'No se pudo actualizar el estado del pedido.';
        this.cdr.detectChanges();
      }
    });
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
