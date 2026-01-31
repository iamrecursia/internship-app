package com.kozitskiy.userservice.dto;


import lombok.Builder;

import java.time.LocalDate;
import java.util.List;


@Builder
public record UserWithCardResponse(
        Long id,
        String name,
        String surname,
        String email,
        LocalDate birthDate,
        List<CardResponse> cards
) {

}
