package com.kozitskiy.authservice.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        //For user-service
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Surname is required")
        @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
        String surname,

        @NotNull(message = "Birth date is required") // Для объектов (не строк) используем @NotNull
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {


}
