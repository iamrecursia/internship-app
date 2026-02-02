package com.kozitskiy.paymentservice.service;

import com.kozitskiy.dto.OrderCreatedEvent;
import com.kozitskiy.dto.PaymentProcessedEvent;
import com.kozitskiy.paymentservice.client.RandomNumberClient;
import com.kozitskiy.paymentservice.dto.PaymentRequest;
import com.kozitskiy.paymentservice.dto.PaymentResponse;
import com.kozitskiy.paymentservice.entity.Payment;
import com.kozitskiy.paymentservice.entity.enums.PaymentStatus;
import com.kozitskiy.paymentservice.exception.BusinessException;
import com.kozitskiy.paymentservice.kafka.producer.PaymentEventProducer;
import com.kozitskiy.paymentservice.mapper.PaymentMapper;
import com.kozitskiy.paymentservice.repository.PaymentRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RandomNumberClient randomNumberClient;
    private final PaymentEventProducer paymentEventProducer;

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for order: {}", request.orderId());

        Payment payment = paymentMapper.toEntity(request);

        // API logic
        try {
            String response = randomNumberClient.getRandomNumber();

            String cleanNumber = response.trim();
            log.info("External API returned: '{}'", cleanNumber);

            int number = Integer.parseInt(cleanNumber);

            if (number % 2 == 0){
                payment.setStatus(PaymentStatus.SUCCESS);
            }else {
                payment.setStatus(PaymentStatus.FAILED);
            }
        }catch (Exception e){
            log.error("Error calling external Random API");
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment saved = paymentRepository.save(payment);
        log.debug("Payment saved with ID: {}", saved.getId());

        return paymentMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> findAllByOrderId(Long orderId) {
        validateId(orderId, "Order ID");
        return paymentMapper.toDTOList(paymentRepository.findAllByOrderId(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> findAllByUserId(Long userId) {
        validateId(userId, "User ID");

        List<Payment> payments = paymentRepository.findAllByUserId(userId);
        return paymentMapper.toDTOList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> findAllByStatuses(List<PaymentStatus> statuses) {
        if (statuses == null){
            return Collections.emptyList();
        }

        List<Payment> payments = paymentRepository.findAllByStatusIn(statuses);
        return paymentMapper.toDTOList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSum(@NonNull Instant start,
                                  @NonNull Instant end,
                                  @NonNull String currency) {
        if (start.isAfter(end)){
            throw new BusinessException("Start date cannot be after end date");
        }

        BigDecimal total = paymentRepository.getTotalSumByPeriod(start, end, currency);

        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public void processOrderPayment(OrderCreatedEvent event) {
        log.info("Processing Kafka event for order: {}", event.orderId());

        Payment payment = Payment.builder()
                .orderId(event.orderId())
                .userId(event.userId())
                .amount(event.amount())
                .currency(event.currency())
                .createdAt(Instant.now())
                .build();

        applyPaymentLogic(payment);

        Payment saved = paymentRepository.save(payment);

        PaymentProcessedEvent resultEvent = new PaymentProcessedEvent(
                saved.getOrderId(),
                saved.getId(),
                saved.getStatus().name()
        );

        paymentEventProducer.sendPaymentResult(resultEvent);

        log.info("Payment saved for orderId: {} with status {}}", event.orderId(), payment.getStatus());
    }

    //------------PRIVATE METHODS-----------------

    private void validateId(final Long id, final String name) {
        if (id == null || id <= 0) {
            log.warn("Invalid {}: {}", name, id);
            throw new BusinessException(name + " must be positive");
        }
    }

    private void applyPaymentLogic(Payment payment) {
        try {
            String response = randomNumberClient.getRandomNumber();
            int number = Integer.parseInt(response.trim());

            if (number % 2 == 0) {
                payment.setStatus(PaymentStatus.SUCCESS);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }
        } catch (Exception e) {
            log.error("Error calling external Random API, failing payment");
            payment.setStatus(PaymentStatus.FAILED);
        }
    }
}
