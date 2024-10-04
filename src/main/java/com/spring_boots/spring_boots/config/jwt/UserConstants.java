package com.spring_boots.spring_boots.config.jwt;

import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * 사용자 토큰 상수 관리
 */
@NoArgsConstructor
public class UserConstants {
    public static final String AUTHORIZATION_TOKEN_KEY = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_TYPE_VALUE = "refresh_token";
    public static final String ACCESS_TOKEN_TYPE_VALUE = "access_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1); // 액세스 토큰 유효 기간
}
