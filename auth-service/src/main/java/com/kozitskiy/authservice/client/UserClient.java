package com.kozitskiy.authservice.client;

import com.kozitskiy.authservice.dto.CreateUserRequest;
import com.kozitskiy.authservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "USER-SERVICE", fallback = UserClientFallback.class)
public interface UserClient {
    @PostMapping("/users")
    ResponseEntity<UserResponseDto> createUser(@RequestBody CreateUserRequest request);
}
