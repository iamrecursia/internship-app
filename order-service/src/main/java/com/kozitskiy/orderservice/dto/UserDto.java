package com.kozitskiy.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
public record UserDto(
        Long id,
        String name,
        String surname,
        String email,
        LocalDate birthDate
) {
}
