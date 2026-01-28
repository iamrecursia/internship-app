package com.kozitskiy.orderservice.repository;

import com.kozitskiy.orderservice.entity.Order;
import com.kozitskiy.orderservice.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
    List<Order> findByStatusIn(Collection<OrderStatus> statuses);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
    List<Order> findAllByIdIn(Collection<Long> ids);

    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") OrderStatus status);
}
