package com.kozitskiy.paymentservice.exception;

public class PaymentNotFoundException extends RuntimeException{
    public PaymentNotFoundException(Long paymentId){
        super(String.format("Payment not found with id: %d", paymentId));
    }
}
