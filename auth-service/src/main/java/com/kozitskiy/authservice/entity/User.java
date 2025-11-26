package com.kozitskiy.authservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;


@Data
@Entity
@Table(name = "auth_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;
}
