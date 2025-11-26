package com.kozitskiy.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private LocalDate birthDate;
}
