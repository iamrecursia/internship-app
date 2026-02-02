package com.kozitskiy.paymentservice.it;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kozitskiy.paymentservice.client.RandomNumberClient;

import com.kozitskiy.paymentservice.dto.PaymentRequest;
import com.kozitskiy.paymentservice.entity.Payment;
import com.kozitskiy.paymentservice.entity.enums.PaymentStatus;
import com.kozitskiy.paymentservice.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@Transactional
public class PaymentControllerIT extends BaseIT{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockitoBean
    private RandomNumberClient randomNumberClient;

    private PaymentRequest.PaymentRequestBuilder requestBuilder;

    @BeforeEach
    void setUp(){
        this.requestBuilder = PaymentRequest.builder()
                .userId(1L)
                .amount(new BigDecimal("250.00"))
                .currency("USD");
    }

    @Test
    void createPayment_shouldSaveToDatabase_whenSuccess() throws Exception{
        Long orderId = 101L;
        PaymentRequest request = requestBuilder.orderId(orderId).build();

        when(randomNumberClient.getRandomNumber()).thenReturn("2");

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.currency").value("USD"));

        Payment saved = paymentRepository.findAllByOrderId(orderId).getFirst();
        assertEquals(PaymentStatus.SUCCESS, saved.getStatus());
        assertEquals(0, new BigDecimal("250.00").compareTo(saved.getAmount()));
    }

    @Test
    void getTotalSum_shouldReturnCorrectSum_whenSuccess() throws Exception{
        Instant now = Instant.now();
        Instant start = now.minusSeconds(3600);
        Instant end = now.plusSeconds(3600);

        paymentRepository.saveAndFlush(createPayment(new BigDecimal("250.0000"), "USD", now));

        paymentRepository.saveAndFlush(createPayment(new BigDecimal("50.5000"), "USD", now));

        paymentRepository.saveAndFlush(createPayment(new BigDecimal("1000.0000"), "EUR", now));

        Payment oldPayment = paymentRepository.save(createPayment(new BigDecimal("99.00"), "USD", now));

        jdbcTemplate.update("UPDATE payments SET created_at = ? WHERE id = ?",
                java.sql.Timestamp.from(now.minusSeconds(7200)), oldPayment.getId());

        mockMvc.perform(get("/api/v1/payments/total")
                .param("start", start.toString())
                .param("end", end.toString())
                .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(content().string("300.5000"));
    }

    private Payment createPayment(BigDecimal amount, String currency, Instant createdAt){
        return Payment.builder()
                .userId(1L)
                .orderId(new java.util.Random().nextLong())
                .currency(currency)
                .amount(amount)
                .status(PaymentStatus.SUCCESS)
                .createdAt(createdAt)
                .build();
    }




}
