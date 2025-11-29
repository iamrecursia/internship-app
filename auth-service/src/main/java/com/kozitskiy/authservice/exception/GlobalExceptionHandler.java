package com.kozitskiy.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.access.AccessDeniedException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<String> handleUserServiceUnavailable(UserServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException e){
        return ResponseEntity.status(403)
                .body(Map.of("error", "Access denied"));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception e) {
        return ResponseEntity.status(500)
                .body(Map.of("error", "Internal server error"));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(RegistrationFailedException.class)
    public ResponseEntity<String> handleRegistrationFailed(RegistrationFailedException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Registration service temporarily unavailable");
    }
}
