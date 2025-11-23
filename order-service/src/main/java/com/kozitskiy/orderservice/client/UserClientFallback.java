package com.kozitskiy.orderservice.client;

import com.kozitskiy.orderservice.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {
    @Override
    public UserDto getUserByEmail(String email){
        UserDto fallback = new UserDto();
        fallback.setEmail(email);
        fallback.setSurname("User unavailable");
        return fallback;
    }
}
