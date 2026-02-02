package com.kozitskiy.orderservice.client;

import com.kozitskiy.orderservice.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {
    @Override
    public UserDto getUserByEmail(String email){

        return UserDto.builder()
                .email(email)
                .surname("User unavailable")
                .build();
    }
}
