package com.kozitskiy.paymentservice.repository;

import com.kozitskiy.paymentservice.entity.Payment;
import com.kozitskiy.paymentservice.dto.PaymentResponse;
import com.kozitskiy.paymentservice.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Long, Payment> {

    List<PaymentResponse> findAllByOrderId(Long orderId);

    List<PaymentResponse> findAllByUserId(Long userId);

    List<PaymentResponse> findAllByStatusIn(List<PaymentStatus> statuses);


    @Query("SELECT COALESCE(SUM(p.amount, 0)) FROM Payment p " +
            "WHERE p.createdAt BETWEEN :start AND :end " +
            "AND p.currency = :currency")
    BigDecimal getTotalSumByPeriod(@Param("start") Instant start,
                                   @Param("end") Instant end,
                                   @Param("currency") String currency);
}
