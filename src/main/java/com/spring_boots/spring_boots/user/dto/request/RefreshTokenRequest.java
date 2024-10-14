package com.spring_boots.spring_boots.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RefreshTokenRequest {
    private String refreshToken;
}
