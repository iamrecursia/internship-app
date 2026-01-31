package com.kozitskiy.userservice.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CardResponse(
        Long id,
        Long userId,
        String number,
        String holder,
        LocalDate expirationDate
) {

}
