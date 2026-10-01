import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface OrderProduct {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
}

export interface OrderItem {
  id: number;
  product: OrderProduct;
  quantity: number;
  unitPrice: number;
}

export type OrderStatus =
  | 'PENDIENTE'
  | 'ACEPTADO'
  | 'PREPARANDO'
  | 'DESPACHADO'
  | 'CANCELADO';

export interface Order {
  id: number;
  customerName: string;
  status: OrderStatus;
  total: number;
  createdAt: string;
  items: OrderItem[];
}

export interface CreateOrderItemPayload {
  quantity: number;
  unitPrice: number;
  product: {
    id: number;
  };
}

export interface CreateOrderPayload {
  customerName: string;
  total: number;
  items: CreateOrderItemPayload[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://pedidos360-backend.us-east-1.elasticbeanstalk.com/api/orders';

  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl);
  }

  getOrder(id: number): Observable<Order> {
    return this.http.get<Order>(
      `${this.apiUrl}/${id}`
    );
  }

  createOrder(order: CreateOrderPayload): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, order);
  }

  updateOrderStatus(
    id: number,
    status: OrderStatus
  ): Observable<Order> {
    const body = new Blob(
      [`"${status}"`],
      { type: 'application/json' }
    );

    return this.http.put<Order>(
      `${this.apiUrl}/${id}/status`,
      body
    );
  }
}

