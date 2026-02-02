package com.kozitskiy.authservice.dto;

import lombok.Builder;

@Builder
public record JwtResponse(
        String accessToken,
        String refreshToken
) {

}
