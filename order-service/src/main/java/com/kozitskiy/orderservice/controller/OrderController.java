package com.kozitskiy.orderservice.controller;

import com.kozitskiy.orderservice.dto.OrderCreateRequest;
import com.kozitskiy.orderservice.dto.OrderResponse;
import com.kozitskiy.orderservice.dto.OrderUpdateRequest;
import com.kozitskiy.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request){
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id){
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersByIds(@RequestParam List<Long> ids){
        List<OrderResponse> responses = orderService.getOrdersByIds(ids);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatuses(@RequestParam List<String> statuses){
        return ResponseEntity.ok(orderService.getOrdersByStatuses(statuses));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderUpdateRequest request){
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

}
