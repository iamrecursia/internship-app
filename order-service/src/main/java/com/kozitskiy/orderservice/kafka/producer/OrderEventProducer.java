package com.kozitskiy.orderservice.kafka.producer;

import com.kozitskiy.dto.KafkaTopics;
import com.kozitskiy.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void sendOrderCreated(OrderCreatedEvent event){
        log.info("Sending OrderCreatedEvent to Kafka: {}", event);

        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, String.valueOf(event.orderId()), event);
    }
}
