package com.kozitskiy.authservice.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super("User with email " + message + " already exists");
    }
}
