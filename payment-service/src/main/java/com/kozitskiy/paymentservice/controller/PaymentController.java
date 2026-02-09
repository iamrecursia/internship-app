package com.kozitskiy.paymentservice.controller;

import com.kozitskiy.paymentservice.dto.PaymentRequest;
import com.kozitskiy.paymentservice.dto.PaymentResponse;
import com.kozitskiy.paymentservice.entity.enums.PaymentStatus;
import com.kozitskiy.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

//    @PostMapping
//    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request){
//        PaymentResponse response = paymentService.createPayment(request);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(@PathVariable Long orderId){

        return ResponseEntity.ok(paymentService.findAllByOrderId(orderId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Long userId){

        return ResponseEntity.ok(paymentService.findAllByUserId(userId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatuses(
            @RequestParam(required = false)List<PaymentStatus> paymentStatuses){

        List<PaymentResponse> responses = paymentService.findAllByStatuses(paymentStatuses);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalSum(@RequestParam Instant start,
                                                  @RequestParam Instant end,
                                                  @RequestParam String currency){
        return ResponseEntity.ok(paymentService.getTotalSum(start, end, currency));
    }





}
