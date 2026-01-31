package com.kozitskiy.userservice.dto;


import lombok.Builder;

import java.time.LocalDate;


@Builder
public record UserResponse(
        Long id,
        String name,
        String surname,
        String email,
        LocalDate birthDate
) {

}
