package com.spring_boots.spring_boots.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JwtTokenLoginRequest {
    private String userRealId;
    private String password;
}
