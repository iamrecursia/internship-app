package com.kozitskiy.paymentservice.service;


import com.kozitskiy.dto.OrderCreatedEvent;
import com.kozitskiy.paymentservice.dto.PaymentRequest;
import com.kozitskiy.paymentservice.dto.PaymentResponse;
import com.kozitskiy.paymentservice.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request);

    List<PaymentResponse> findAllByOrderId(Long orderId);

    List<PaymentResponse> findAllByUserId(Long userId);

    List<PaymentResponse> findAllByStatuses(List<PaymentStatus> statuses);

    BigDecimal getTotalSum(Instant start, Instant end, String currency);

    void processOrderPayment(OrderCreatedEvent orderCreatedEvent);

}
