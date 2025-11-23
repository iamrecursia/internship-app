package com.kozitskiy.orderservice.repository;

import com.kozitskiy.orderservice.entity.Order;
import com.kozitskiy.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusIn(Collection<OrderStatus> statuses);
}
