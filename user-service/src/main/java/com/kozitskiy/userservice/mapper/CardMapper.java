package com.kozitskiy.userservice.mapper;

import com.kozitskiy.userservice.dto.CardRequest;
import com.kozitskiy.userservice.dto.CardResponse;
import com.kozitskiy.userservice.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target = "user", ignore = true)
    Card toEntity(CardRequest dto);

    @Mapping(source = "user.id", target = "userId")
    CardResponse toDto(Card card);

    List<CardResponse> toDtoList(List<Card> card);

    @Mapping(target = "user", ignore = true)
    void updateFromDto(CardRequest dto, @MappingTarget Card card);
}
