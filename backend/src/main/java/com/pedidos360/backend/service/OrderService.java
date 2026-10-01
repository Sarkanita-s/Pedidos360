package com.pedidos360.backend.service;

import com.pedidos360.backend.exception.InsufficientStockException;
import com.pedidos360.backend.exception.InvalidOrderStatusException;
import com.pedidos360.backend.model.Order;
import com.pedidos360.backend.model.OrderItem;
import com.pedidos360.backend.model.OrderStatus;
import com.pedidos360.backend.model.Product;
import com.pedidos360.backend.repository.OrderRepository;
import com.pedidos360.backend.repository.ProductRepository;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<Order> getAllOrders(Jwt jwt) {

        if (hasRole(jwt, "Admin") || hasRole(jwt, "Operador")) {
            return orderRepository.findAll();
        }

        return orderRepository.findByOwnerOid(getOwnerOid(jwt));
    }

    public Order getOrderById(Long id, Jwt jwt) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (hasRole(jwt, "Admin") || hasRole(jwt, "Operador")) {
            return order;
        }

        if (!getOwnerOid(jwt).equals(order.getOwnerOid())) {
            throw new RuntimeException(
                    "No tienes permiso para ver este pedido");
        }

        return order;
    }

    public Order createOrder(Order order, Jwt jwt) {

        order.setStatus(OrderStatus.PENDIENTE);
        order.setOwnerOid(getOwnerOid(jwt));

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(
            Long id,
            OrderStatus newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.DESPACHADO
                || currentStatus == OrderStatus.CANCELADO) {

            throw new InvalidOrderStatusException(
                    "El pedido ya se encuentra en un estado final");
        }

        boolean validTransition = switch (currentStatus) {

            case PENDIENTE ->
                newStatus == OrderStatus.ACEPTADO
                        || newStatus == OrderStatus.CANCELADO;

            case ACEPTADO ->
                newStatus == OrderStatus.PREPARANDO
                        || newStatus == OrderStatus.CANCELADO;

            case PREPARANDO ->
                newStatus == OrderStatus.DESPACHADO
                        || newStatus == OrderStatus.CANCELADO;

            default -> false;
        };

        if (!validTransition) {
            throw new InvalidOrderStatusException(
                    "Transición no permitida: "
                            + currentStatus + " -> " + newStatus);
        }

        if (newStatus == OrderStatus.ACEPTADO
                && currentStatus != OrderStatus.ACEPTADO) {

            for (OrderItem item : order.getItems()) {

                Long productId = item.getProduct().getId();

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException(
                                "Producto no encontrado: "
                                        + productId));

                if (product.getStock() < item.getQuantity()) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para el producto: "
                                    + product.getName());
                }

                product.setStock(
                        product.getStock() - item.getQuantity());

                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);

        return orderRepository.save(order);
    }

    private String getOwnerOid(Jwt jwt) {

        String oid = jwt.getClaimAsString("oid");

        if (oid != null && !oid.isBlank()) {
            return oid;
        }

        return jwt.getSubject();
    }

    private boolean hasRole(Jwt jwt, String role) {

        Object roles = jwt.getClaims().get("roles");

        if (!(roles instanceof List<?> roleList)) {
            return false;
        }

        return roleList.stream()
                .anyMatch(r -> role.equals(r.toString()));
    }
}
