package com.spring_boots.spring_boots.user.dto.request;

import com.spring_boots.spring_boots.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class JwtTokenDto {
    private String accessToken;
    private String refreshToken;
    private UserRole role;
}
