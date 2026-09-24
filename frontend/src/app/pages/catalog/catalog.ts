
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

  createProduct(): void {
    if (
      !this.newProduct.name.trim() ||
      this.newProduct.price <= 0 ||
      this.newProduct.stock < 0
    ) {
      this.error = 'Completa los datos correctamente.';
      return;
    }

    this.saving = true;
    this.error = '';

    console.log('ID en edición:', this.editingProductId);

    if (this.editingProductId !== null) {
      // ACTUALIZAR producto existente
      this.productService.updateProduct(
        this.editingProductId,
        this.newProduct
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
      this.productService.createProduct(this.newProduct).subscribe({
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

    this.newProduct = {
      name: '',
      description: '',
      price: 0,
      stock: 0
    };

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
  }
}