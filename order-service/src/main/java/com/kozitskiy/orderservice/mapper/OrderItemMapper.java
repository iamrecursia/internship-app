package com.kozitskiy.orderservice.mapper;

import com.kozitskiy.orderservice.dto.OrderItemResponse;
import com.kozitskiy.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.name", target = "itemName")
    @Mapping(source = "item.price", target = "itemPrice")
    @Mapping(source = "quantity", target = "quantity")
    OrderItemResponse toDto(OrderItem orderItem);

}
