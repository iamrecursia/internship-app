package com.kozitskiy.userservice.exception;

import com.kozitskiy.userservice.dto.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFound(UserNotFoundException ex){
        log.warn("User not found: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationExceptions(MethodArgumentNotValidException ex){
        return buildResponse(ex.getMessage(), "VALIDATION_EXCEPTION", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFound(CardNotFoundException ex){
        log.warn("Card not found: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), "CARD_NOT_FOUND", HttpStatus.NOT_FOUND);
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
