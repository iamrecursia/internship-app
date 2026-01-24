package com.kozitskiy.paymentservice.exception.handler;

import com.kozitskiy.paymentservice.dto.error.ErrorDto;
import com.kozitskiy.paymentservice.exception.BusinessException;
import com.kozitskiy.paymentservice.exception.PaymentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    // Handling 404 not found
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorDto> handlePaymentNotFound(PaymentNotFoundException ex) {
        log.warn("Payment not found: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), "PAYMENT_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDto> handleBusinessException(BusinessException ex) {
        log.warn("Business error: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), "BUSINESS_LOGIC_ERROR", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGeneralException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return buildResponse("An internal server error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorDto> buildResponse(String message, String code, HttpStatus status) {
        ErrorDto error = ErrorDto.builder()
                .message(message)
                .errorCode(code)
                .timestamp(Instant.now())
                .build();
        return new ResponseEntity<>(error, status);
    }
}
