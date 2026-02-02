package com.kozitskiy.orderservice.mapper;

import com.kozitskiy.dto.OrderCreatedEvent;
import com.kozitskiy.orderservice.dto.OrderCreateRequest;
import com.kozitskiy.orderservice.dto.OrderResponse;
import com.kozitskiy.orderservice.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        uses = {OrderItemMapper.class}
)
public interface OrderMapper {

    @Mapping(source = "orderItems", target = "items")
    @Mapping(target = "user", ignore = true)
    OrderResponse toDto(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(OrderCreateRequest request);


    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "amount", source = "totalAmount")
    @Mapping(target = "currency", constant = "USD")
    OrderCreatedEvent toEvent(Order order, BigDecimal totalAmount);
}
