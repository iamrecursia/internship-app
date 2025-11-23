package com.kozitskiy.orderservice.mapper;

import com.kozitskiy.orderservice.dto.OrderResponse;
import com.kozitskiy.orderservice.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
        componentModel = "spring",
        uses = OrderItemMapper.class// когда мапишь Order и там встречается List<OrderItem>
)
public interface OrderMapper {

    @Mapping(source = "status", target = "status", qualifiedByName = "enumToString")
    @Mapping(source = "userEmail", target = "userEmail")
    @Mapping(source = "orderItems", target = "items")
    OrderResponse toDto(Order order);

    @Named("enumToString")
    default String enumToString(Enum<?> e) {
        return e != null ? e.name() : null;
    }
}
