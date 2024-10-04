package com.spring_boots.spring_boots.user.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtTokenDto {
    private String accessToken;
    private String refreshToken;
}
