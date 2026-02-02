package com.kozitskiy.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
public record UserResponseDto(
        Long id,
        String name,
        String surname,
        String email,
        LocalDate birthDate
) {

}
