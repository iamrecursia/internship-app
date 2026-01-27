package com.kozitskiy.paymentservice.kafka.consumer;

import com.kozitskiy.dto.OrderCreatedEvent;
import com.kozitskiy.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-created-topic", groupId = "payment-service-group")
    public void handleOrderCreated(OrderCreatedEvent event){
        log.info("Received order created event from Kafka: {}", event);
        try {
            paymentService.processOrderPayment(event);
        }catch (Exception e){
            log.error("Failed to process payment for orderId: {}", event.orderId(), e);
        }
    }

}
