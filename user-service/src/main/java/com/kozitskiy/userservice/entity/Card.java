package com.kozitskiy.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@Table(name = "card_info")
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Card{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 16)
    private String number;

    @Column(nullable = false)
    private String holder;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return number != null && number.equals(card.getNumber());
    }

    @PrePersist
    protected void onCreate() {
        java.time.Instant now = java.time.Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (holder != null) {
            this.holder = holder.toUpperCase().trim();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.Instant.now();
        if (holder != null) {
            this.holder = holder.toUpperCase().trim();
        }
    }

    @Override
    public int hashCode() {
        return number != null ? number.hashCode() : getClass().hashCode();
    }
}
