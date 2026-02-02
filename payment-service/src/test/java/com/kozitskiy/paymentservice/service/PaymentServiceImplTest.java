package com.kozitskiy.paymentservice.service;

import com.kozitskiy.paymentservice.dto.PaymentResponse;
import com.kozitskiy.paymentservice.entity.Payment;
import com.kozitskiy.paymentservice.entity.enums.PaymentStatus;
import com.kozitskiy.paymentservice.exception.BusinessException;
import com.kozitskiy.paymentservice.mapper.PaymentMapper;
import com.kozitskiy.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private PaymentResponse paymentDto;

    @BeforeEach
    void setUp() {

        Instant fixedTime = Instant.now();

        payment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .userId(1L)
                .status(PaymentStatus.SUCCESS)
                .amount(BigDecimal.valueOf(250))
                .currency("USD")
                .createdAt(Instant.now())
                .build();

        paymentDto = PaymentResponse.builder()
                .id(1L)
                .orderId(1L)
                .userId(1L)
                .status(PaymentStatus.SUCCESS)
                .amount(BigDecimal.valueOf(250))
                .currency("USD")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void testFindByOrderId_ShouldReturnList() {

        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(payment));
        when(paymentMapper.toDTOList(List.of(payment))).thenReturn(List.of(paymentDto));

        List<PaymentResponse> paymentResponseList = paymentService.findAllByOrderId(1L);

        assertNotNull(paymentResponseList);
        assertEquals(1, paymentResponseList.size());
        assertEquals(PaymentStatus.SUCCESS, paymentResponseList.getFirst().status());
        assertEquals(BigDecimal.valueOf(250), paymentResponseList.getFirst().amount());

        verify(paymentMapper, times(1)).toDTOList(List.of(payment));
        verify(paymentRepository, times(1)).findAllByOrderId(1L);
    }

    @Test
    void testFindAllByUserId_ShouldReturnList() {
        when(paymentRepository.findAllByUserId(1L)).thenReturn(List.of(payment));
        when(paymentMapper.toDTOList(List.of(payment))).thenReturn(List.of(paymentDto));

        List<PaymentResponse> paymentResponseList = paymentService.findAllByUserId(1L);

        assertNotNull(paymentResponseList);
        assertEquals(1, paymentResponseList.size());
        assertEquals(PaymentStatus.SUCCESS, paymentResponseList.getFirst().status());
        assertEquals(BigDecimal.valueOf(250), paymentResponseList.getFirst().amount());

        verify(paymentMapper, times(1)).toDTOList(List.of(payment));
        verify(paymentRepository, times(1)).findAllByUserId(1L);
    }

    @Test
    void testFindAllByStatuses_ShouldReturnAList() {

        when(paymentRepository.findAllByStatusIn(List.of(
                PaymentStatus.SUCCESS, PaymentStatus.FAILED))).thenReturn(List.of(payment));

        when(paymentMapper.toDTOList(List.of(payment))).thenReturn(List.of(paymentDto));

        List<PaymentResponse> paymentResponseList
                = paymentService.findAllByStatuses(List.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED));

        assertNotNull(paymentResponseList);
        assertEquals(1, paymentResponseList.size());
        assertNotEquals(PaymentStatus.FAILED, paymentResponseList.getFirst().status());
        assertEquals(PaymentStatus.SUCCESS, paymentResponseList.getFirst().status());

        verify(paymentMapper, times(1)).toDTOList(List.of(payment));
        verify(paymentRepository, times(1)).findAllByStatusIn(List.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED));
    }

    @Test
    void testGetTotalSum_ShouldReturnBiDecimalValue() {

        Instant start = Instant.now();
        Instant end = start.plusSeconds(600);
        String currency = "USD";

        when(paymentRepository
                .getTotalSumByPeriod(start, end, currency))
                .thenReturn(BigDecimal.valueOf(500));

        BigDecimal testSum = paymentService
                .getTotalSum(start, end, currency);

        assertEquals(testSum, BigDecimal.valueOf(500));

        verify(paymentRepository, times(1))
                .getTotalSumByPeriod(start, end, currency);
    }

    @Test
    void testFindByOrderId_InvalidId_ShouldReturnAnException() {
        BusinessException exNull = assertThrows(BusinessException.class,
                () -> paymentService.findAllByOrderId(null));
        assertEquals("Order ID must be positive", exNull.getMessage());

        BusinessException exNeg = assertThrows(BusinessException.class,
                () -> paymentService.findAllByOrderId(-5L));
        assertEquals("Order ID must be positive", exNeg.getMessage());

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void testFindAllByUserId_InvalidId_ShouldThrowException() {
        assertThrows(BusinessException.class, () -> paymentService.findAllByUserId(0L));
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void testFindAllByStatuses_NullStatuses_ShouldThrownException() {
        List<PaymentResponse> result = paymentService.findAllByStatuses(null);

        assertTrue(result.isEmpty());

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void testGetTotalSum_StartAfterEnd_ShouldThrowException() {
        Instant start = Instant.now();
        Instant end = start.minusSeconds(600);

        assertThrows(BusinessException.class,
                () -> paymentService.getTotalSum(start, end, "USD"));

        verifyNoInteractions(paymentRepository);
    }


}
