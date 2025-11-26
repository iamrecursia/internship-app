package com.kozitskiy.userservice.util;

import com.kozitskiy.userservice.dto.request.CreateCardDto;
import com.kozitskiy.userservice.dto.response.CardResponseDto;
import com.kozitskiy.userservice.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target = "user", ignore = true)
    Card toEntity(CreateCardDto dto);

    @Mapping(source = "user.id", target = "userId")
    CardResponseDto toDto(Card card);

    List<CardResponseDto> toDtoList(List<Card> card);

    @Mapping(target = "user", ignore = true)
    void updateFromDto(CreateCardDto dto, @MappingTarget Card card);
}
