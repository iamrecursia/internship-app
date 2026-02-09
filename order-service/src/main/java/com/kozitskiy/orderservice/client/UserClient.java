package com.kozitskiy.orderservice.client;

import com.kozitskiy.orderservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("api/v1/users/search")
    UserDto getUserByEmail(@RequestParam("email") String email);
}
