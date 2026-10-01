package com.pedidos360.backend.controller;

import com.pedidos360.backend.model.Order;
import com.pedidos360.backend.model.OrderStatus;
import com.pedidos360.backend.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'Operador', 'Cliente')")
    public ResponseEntity<List<Order>> getAllOrders(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                orderService.getAllOrders(jwt));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'Operador', 'Cliente')")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                orderService.getOrderById(id, jwt));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'Operador', 'Cliente')")
    public ResponseEntity<Order> createOrder(
            @RequestBody Order order,
            @AuthenticationPrincipal Jwt jwt) {

        Order createdOrder = orderService.createOrder(order, jwt);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdOrder);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Admin', 'Operador')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatus newStatus) {

        Order updatedOrder = orderService.updateOrderStatus(id, newStatus);

        return ResponseEntity.ok(updatedOrder);
    }

}