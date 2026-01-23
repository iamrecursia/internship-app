package com.kozitskiy.paymentservice.service;

import com.kozitskiy.paymentservice.dto.PaymentRequest;
import com.kozitskiy.paymentservice.dto.PaymentResponse;
import com.kozitskiy.paymentservice.entity.enums.PaymentStatus;
import com.kozitskiy.paymentservice.mapper.PaymentMapper;
import com.kozitskiy.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;


    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        return null;
    }

    @Override
    public List<PaymentResponse> findAllByOrderId(Long orderId) {
        return List.of();
    }

    @Override
    public List<PaymentResponse> findAllByUserId(Long userId) {
        return List.of();
    }

    @Override
    public List<PaymentResponse> findAllByStatuses(List<PaymentStatus> statuses) {
        return List.of();
    }

    @Override
    public BigDecimal getTotalSum(Instant start, Instant end, String currency) {
        return null;
    }
}
