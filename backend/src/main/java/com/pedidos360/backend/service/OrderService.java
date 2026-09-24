package com.pedidos360.backend.service;

import com.pedidos360.backend.exception.InvalidOrderStatusException;
import com.pedidos360.backend.model.Order;
import com.pedidos360.backend.model.OrderStatus;
import com.pedidos360.backend.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    public Order createOrder(Order order) {
        order.setStatus(OrderStatus.PENDIENTE);
        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long id, OrderStatus newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        OrderStatus currentStatus = order.getStatus();

        if (newStatus == OrderStatus.DESPACHADO
                && currentStatus != OrderStatus.ACEPTADO
                && currentStatus != OrderStatus.PREPARANDO) {

            throw new InvalidOrderStatusException(
                    "El pedido debe estar ACEPTADO o PREPARANDO antes de ser DESPACHADO");
        }

        order.setStatus(newStatus);

        return orderRepository.save(order);
    }
}