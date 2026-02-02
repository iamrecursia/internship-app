package com.kozitskiy.userservice.repository;

import com.kozitskiy.userservice.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findCardsByUserId(Long userId, Pageable pageable);
}
