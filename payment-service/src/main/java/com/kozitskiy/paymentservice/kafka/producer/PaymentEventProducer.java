package com.kozitskiy.paymentservice.kafka.producer;

import com.kozitskiy.dto.KafkaTopics;
import com.kozitskiy.dto.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

    public void sendPaymentResult(PaymentProcessedEvent event){
        log.info("Sending payment result to Kafka for order: {}", event.orderId());

        kafkaTemplate.send(KafkaTopics.PAYMENT_RESULT, String.valueOf(event.orderId()), event);
    }

}
