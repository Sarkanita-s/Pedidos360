import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { OrderService, Order } from '../../services/order';
import { ProductService, Product } from '../../services/product';
import { forkJoin } from 'rxjs';

export interface DashboardStats {
  totalOrders: number;
  pendingOrders: number;
  activeOrders: number;    // ACEPTADO + PREPARANDO
  dispatchedOrders: number;
  totalProducts: number;
}

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly productService = inject(ProductService);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = true;
  error = '';

  stats: DashboardStats = {
    totalOrders: 0,
    pendingOrders: 0,
    activeOrders: 0,
    dispatchedOrders: 0,
    totalProducts: 0
  };

  recentOrders: Order[] = [];
  products: Product[] = [];

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.error = '';

    forkJoin({
      orders: this.orderService.getOrders(),
      products: this.productService.getProducts()
    }).subscribe({
      next: ({ orders, products }) => {
        this.products = products;
        this.recentOrders = [...orders]
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
          .slice(0, 5);

        this.stats = {
          totalOrders: orders.length,
          pendingOrders: orders.filter(o => o.status === 'PENDIENTE').length,
          activeOrders: orders.filter(
            o => o.status === 'ACEPTADO' || o.status === 'PREPARANDO'
          ).length,
          dispatchedOrders: orders.filter(o => o.status === 'DESPACHADO').length,
          totalProducts: products.length
        };

        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error('Error al cargar datos del dashboard:', err);
        this.error = 'No se pudieron cargar los datos del panel. Verifica que el servidor esté activo.';
        this.loading = false;
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

  getStatusClass(status: string): string {
    return 'status-' + status.toLowerCase();
  }
}
