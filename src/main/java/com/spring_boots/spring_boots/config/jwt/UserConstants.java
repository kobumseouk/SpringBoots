package com.spring_boots.spring_boots.config.jwt;

import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * 사용자 토큰 상수 관리
 */
@NoArgsConstructor
public class UserConstants {
    public static final String REFRESH_TOKEN_TYPE_VALUE = "refreshToken"; //리프레시 토큰 이름
    public static final String ACCESS_TOKEN_TYPE_VALUE = "accessToken"; //엑세스토큰이름
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);  //리프레시토큰 유호기간
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(15); // 액세스 토큰 유효 기간
}
