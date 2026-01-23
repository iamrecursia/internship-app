package com.kozitskiy.paymentservice.mapper;


import com.kozitskiy.paymentservice.dto.PaymentRequest;
import com.kozitskiy.paymentservice.dto.PaymentResponse;
import com.kozitskiy.paymentservice.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createAt", ignore = true)
    Payment toEntity(PaymentRequest dto);

    PaymentResponse toDTO(Payment payment);

    List<PaymentResponse> toDTOList(List<Payment> payments);

}
