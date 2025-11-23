package com.kozitskiy.orderservice.controller;

import com.kozitskiy.orderservice.dto.OrderCreateRequest;
import com.kozitskiy.orderservice.dto.OrderResponse;
import com.kozitskiy.orderservice.dto.OrderUpdateRequest;
import com.kozitskiy.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request){
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id){
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersByIds(@RequestParam List<Long> ids){
        List<OrderResponse> responses = orderService.getOrdersByIds(ids);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("by-status")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatuses(@RequestParam List<String> statuses){
        List<OrderResponse> responses = orderService.getOrdersByStatuses(statuses);
        return ResponseEntity.ok(responses);
    }


    @PatchMapping("{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id,
                                                           @Valid @RequestBody OrderUpdateRequest request){
        OrderResponse response = orderService.updateOrder(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }




}
