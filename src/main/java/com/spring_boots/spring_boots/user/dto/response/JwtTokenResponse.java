package com.spring_boots.spring_boots.user.dto.response;

import com.spring_boots.spring_boots.user.domain.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtTokenResponse {
    private String accessToken;
    private String refreshToken;
    private boolean isAdmin;
}
