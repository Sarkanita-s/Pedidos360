import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { Orders } from './orders';
import { Order } from '../../services/order';

describe('Orders Component', () => {
  let component: Orders;
  let fixture: ComponentFixture<Orders>;

  const mockOrder: Order = {
    id: 99,
    customerName: 'Cliente Test',
    status: 'PENDIENTE',
    total: 15000,
    createdAt: '2026-09-24T12:00:00.000Z',
    items: [
      {
        id: 1,
        product: {
          id: 2,
          name: 'Pizza Test',
          description: 'Pizza de prueba',
          price: 15000,
          stock: 10
        },
        quantity: 1,
        unitPrice: 15000
      }
    ]
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Orders],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Orders);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open order detail', () => {
    expect(component.selectedOrder).toBeNull();
    component.openDetail(mockOrder);
    expect(component.selectedOrder).toEqual(mockOrder);
  });

  it('should close order detail', () => {
    component.openDetail(mockOrder);
    expect(component.selectedOrder).not.toBeNull();
    component.closeDetail();
    expect(component.selectedOrder).toBeNull();
  });

  it('should close order detail on escape key', () => {
    component.openDetail(mockOrder);
    component.onEscape();
    expect(component.selectedOrder).toBeNull();
  });
});