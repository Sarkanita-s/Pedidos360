
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService, Product } from '../../services/product';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-catalog',
  imports: [CommonModule, FormsModule],
  templateUrl: './catalog.html',
  styleUrl: './catalog.scss'
})
export class Catalog implements OnInit {

  private readonly productService = inject(ProductService);
  private readonly cdr = inject(ChangeDetectorRef);

  products: Product[] = [];
  loading = true;
  error = '';

  ngOnInit(): void {
    console.log('Catalog iniciado');
    this.loadProducts();
  }

  getTotalStock(): number {
    return this.products.reduce(
      (total, product) => total + product.stock,
      0
    );
  }

  loadProducts(): void {
    console.log('Solicitando productos al backend');

    this.loading = true;
    this.error = '';

    this.productService.getProducts().subscribe({
      next: (products) => {
        console.log('Productos recibidos:', products);

        this.products = products;
        this.loading = false;

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar productos:', error);

        this.error = 'No se pudieron cargar los productos.';
        this.loading = false;

        this.cdr.detectChanges();
      }
    });
  }

  showForm = false;
  saving = false;
  editingProductId: number | null = null;

  newProduct = {
    name: '',
    description: '',
    price: 0,
    stock: 0
  };

  toggleForm(): void {
    if (this.showForm && this.editingProductId === null) {
      this.cancelForm();
    } else {
      this.openCreateForm();
    }
  }

  openCreateForm(): void {
    this.editingProductId = null;
    this.resetProduct();
    this.showForm = true;
    this.error = '';
    this.cdr.detectChanges();
  }

  openForm(): void {
    this.openCreateForm();
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingProductId = null;
    this.error = '';
    this.resetProduct();
    this.cdr.detectChanges();
  }

  closeForm(): void {
    this.cancelForm();
  }

  resetProduct(): void {
    this.newProduct = {
      name: '',
      description: '',
      price: 0,
      stock: 0
    };
  }

  saveProduct(): void {
    this.createProduct();
  }

  createProduct(): void {
    const name = this.newProduct.name?.trim();
    const price = Number(this.newProduct.price);
    const stock = Number(this.newProduct.stock);

    if (
      !name ||
      this.newProduct.price === null ||
      this.newProduct.price === undefined ||
      isNaN(price) ||
      price <= 0 ||
      this.newProduct.stock === null ||
      this.newProduct.stock === undefined ||
      isNaN(stock) ||
      stock < 0
    ) {
      this.error = 'Completa los datos correctamente.';
      this.cdr.detectChanges();
      return;
    }

    this.saving = true;
    this.error = '';

    console.log('ID en edición:', this.editingProductId);

    const payload = {
      name: name,
      description: this.newProduct.description?.trim() || '',
      price: price,
      stock: stock
    };

    if (this.editingProductId !== null) {
      // ACTUALIZAR producto existente
      this.productService.updateProduct(
        this.editingProductId,
        payload
      ).subscribe({
        next: () => {
          console.log('Producto actualizado correctamente');
          this.finishSaving();
        },
        error: (error) => {
          console.error('Error al actualizar:', error);
          this.error = 'No se pudo actualizar el producto.';
          this.saving = false;
          this.cdr.detectChanges();
        }
      });

    } else {
      // CREAR producto nuevo
      this.productService.createProduct(payload).subscribe({
        next: () => {
          console.log('Producto creado correctamente');
          this.finishSaving();
        },
        error: (error) => {
          console.error('Error al crear:', error);
          this.error = 'No se pudo crear el producto.';
          this.saving = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  finishSaving(): void {
    this.saving = false;
    this.showForm = false;
    this.editingProductId = null;

    this.resetProduct();

    this.loadProducts();
  }

  deleteProduct(id: number): void {
    const confirmed = confirm(
      '¿Estás segura de que quieres eliminar este producto?'
    );

    if (!confirmed) {
      return;
    }

    this.productService.deleteProduct(id).subscribe({
      next: () => {
        this.loadProducts();
      },
      error: (error) => {
        console.error('Error al eliminar producto:', error);
        this.error = 'No se pudo eliminar el producto.';
        this.cdr.detectChanges();
      }
    });
  }

  editProduct(product: Product): void {
    this.editingProductId = product.id;

    this.newProduct = {
      name: product.name,
      description: product.description,
      price: product.price,
      stock: product.stock
    };

    this.showForm = true;
    this.error = '';

    console.log('Editando producto con ID:', this.editingProductId);
    this.cdr.detectChanges();
  }
}