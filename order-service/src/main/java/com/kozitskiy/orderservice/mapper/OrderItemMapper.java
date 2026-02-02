package com.kozitskiy.orderservice.mapper;

import com.kozitskiy.orderservice.dto.OrderItemRequest;
import com.kozitskiy.orderservice.dto.OrderItemResponse;
import com.kozitskiy.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.name", target = "itemName")
    @Mapping(source = "item.price", target = "itemPrice")
    OrderItemResponse toDto(OrderItem orderItem); // ← исправлено с Order на OrderItem

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "item", ignore = true) // Item устанавливается отдельно
    OrderItem toEntity(OrderItemRequest request);

}
