package com.kozitskiy.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDate;


@Builder
public record CardRequest(
        @NotNull(message = "User ID is required")
        @Min(value = 1, message = "User ID must be positive")
        Long userId,

        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String number,

        @NotBlank(message = "Cardholder name is required")
        @Size(max = 100)
        String holder,

        @NotNull(message = "Expiration date is required")
        @Future(message = "Expiration date must be in the future")
        LocalDate expirationDate
) {


}
