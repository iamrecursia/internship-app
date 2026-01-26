package com.kozitskiy.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kozitskiy.paymentservice.dto.PaymentRequest;
import com.kozitskiy.paymentservice.dto.PaymentResponse;
import com.kozitskiy.paymentservice.entity.enums.PaymentStatus;
import com.kozitskiy.paymentservice.exception.BusinessException;
import com.kozitskiy.paymentservice.exception.PaymentNotFoundException;
import com.kozitskiy.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    private PaymentRequest validRequest;
    private PaymentResponse defaultResponse;
    private PaymentRequest invalidRequest;

    @BeforeEach
    void setUp() {
        validRequest = PaymentRequest.builder()
                .userId(1L)
                .orderId(1L)
                .amount(BigDecimal.valueOf(250))
                .currency("USD")
                .build();

        defaultResponse = PaymentResponse.builder()
                .id(1L)
                .orderId(1L)
                .userId(1L)
                .status(PaymentStatus.SUCCESS)
                .amount(BigDecimal.valueOf(250))
                .currency("USD")
                .createdAt(Instant.now())
                .build();

        invalidRequest = PaymentRequest.builder()
                .userId(1L)
                .orderId(1L)
                .amount(BigDecimal.valueOf(-100))
                .currency("USD")
                .build();
    }

    @Test
    void createPayment_shouldReturnCreated() throws Exception {

        when(paymentService.createPayment(any(PaymentRequest.class))).thenReturn(defaultResponse);

        mockMvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getPaymentsByOrderId_shouldReturnList() throws Exception{

        when(paymentService.findAllByOrderId(1L)).thenReturn(List.of(defaultResponse));

        mockMvc.perform(get("/api/v1/payments/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1L));
    }

    @Test
    void getPaymentsByUserId_shouldReturnList() throws Exception{

        when(paymentService.findAllByUserId(1L)).thenReturn(List.of(defaultResponse));

        mockMvc.perform(get("/api/v1/payments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1L));
    }

    @Test
    void getPaymentsByStatuses_shouldReturnList() throws Exception{

        when(paymentService.findAllByStatuses(List.of(PaymentStatus.SUCCESS))).thenReturn(List.of(defaultResponse));

        mockMvc.perform(get("/api/v1/payments/filter")
                        .param("paymentStatuses", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void getTotalSum_shouldReturnBigDecimal() throws Exception{

        Instant start = Instant.parse("2024-01-01T10:00:00Z");
        Instant end = Instant.parse("2024-01-01T11:00:00Z");
        String currency = "USD";
        BigDecimal total = BigDecimal.valueOf(500);

        when(paymentService.getTotalSum(start, end, currency)).thenReturn(total);

        mockMvc.perform(get("/api/v1/payments/total")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("currency", currency))
                .andExpect(status().isOk())
                .andExpect(content().string("500"));
    }

    @Test
    void getPaymentsByOrderId_shouldReturn404_whenNotFound() throws Exception{

        Long missingId = 999L;
        when(paymentService.findAllByOrderId(missingId)).thenThrow(new PaymentNotFoundException(missingId));

        mockMvc.perform(get("/api/v1/payments/order/{orderId}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_NOT_FOUND"));
    }

    @Test
    void getPaymentsByUserId_shouldReturn404_whenNotFound() throws Exception{

        Long missingId = 999L;
        when(paymentService.findAllByOrderId(missingId)).thenThrow(new PaymentNotFoundException(missingId));

        mockMvc.perform(get("/api/v1/payments/order/{userId}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_NOT_FOUND"));
    }

    @Test
    void createPayment_shouldReturn400_whenAmountIsNegative() throws Exception{

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTotalSum_shouldReturn400_whenDataAreInvalid() throws Exception{

        Instant start = Instant.parse("2024-01-02T10:00:00Z");
        Instant end = Instant.parse("2024-01-01T10:00:00Z");
        String currency = "USD";

        when(paymentService.getTotalSum(start, end, currency))
                .thenThrow(new BusinessException("Start date cannot be after end date"));

        mockMvc.perform(get("/api/v1/payments/total")
                .param("start", start.toString())
                .param("end", end.toString())
                .param("currency", currency))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_LOGIC_ERROR"));
    }



}
