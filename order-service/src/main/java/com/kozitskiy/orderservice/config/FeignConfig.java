package com.kozitskiy.orderservice.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.kozitskiy.orderservice.client")
public class FeignConfig {
}
