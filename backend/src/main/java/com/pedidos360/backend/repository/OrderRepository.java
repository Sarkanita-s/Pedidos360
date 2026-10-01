package com.pedidos360.backend.repository;

import com.pedidos360.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOwnerOid(String ownerOid);
}
