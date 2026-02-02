package com.kozitskiy.orderservice.entity;

import com.kozitskiy.orderservice.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Builder.Default
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY,
            orphanRemoval = true, cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem item){
        orderItems.add(item);
        item.setOrder(this);
    }

    public BigDecimal getTotalAmount(){
        return orderItems.stream()
                .map(io -> io.getItem().getPrice().multiply(BigDecimal.valueOf(io.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString(){
        return "Order(id=" + id + ", status=" + status + ", email=" + userEmail + ")";
    }
}
