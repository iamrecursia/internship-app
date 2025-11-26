package com.kozitskiy.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWithCardResponseDto {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private LocalDate birthDate;
    private List<CardResponseDto> cards;
}
