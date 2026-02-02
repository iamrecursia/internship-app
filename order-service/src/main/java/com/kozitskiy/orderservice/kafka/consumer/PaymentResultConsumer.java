package com.kozitskiy.orderservice.kafka.consumer;

import com.kozitskiy.dto.KafkaTopics;
import com.kozitskiy.dto.PaymentProcessedEvent;
import com.kozitskiy.orderservice.entity.OrderStatus;
import com.kozitskiy.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = KafkaTopics.PAYMENT_RESULT, groupId = "order-service-group")
    public void handlePaymentResult(PaymentProcessedEvent event){
        log.info("Received payment result for order {}: {}", event.orderId(), event.status());

        orderRepository.findById(event.orderId()).ifPresent(order -> {
            if("SUCCESS".equals(event.status())){
                order.setStatus(OrderStatus.COMPLETED);
            }else {
                order.setStatus(OrderStatus.CANCELLED);
            }

            orderRepository.save(order);
            log.info("Order {} status updated to {}", order.getId(), order.getStatus());
        });
    }
}
