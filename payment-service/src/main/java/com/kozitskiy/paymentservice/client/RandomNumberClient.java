package com.kozitskiy.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "random-number-client", url = "${external.random-api.url}")
public interface RandomNumberClient {

    @GetMapping
    String getRandomNumber();
}
