package com.kozitskiy.orderservice.client;

import com.kozitskiy.orderservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/api/v1/users/email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);
}
