package com.kozitskiy.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDate;


@Builder
public record UserRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Surname is required")
        String surname,

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email
) {

}
