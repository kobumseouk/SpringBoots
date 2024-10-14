package com.spring_boots.spring_boots.user.service;

import com.spring_boots.spring_boots.config.jwt.TokenProvider;
import com.spring_boots.spring_boots.user.domain.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public String createNewAccessToken(String refreshToken) {
        // 토큰 유효성 검사에 실패하면 예외 발생
        if(!tokenProvider.validToken(refreshToken)) {
            throw new IllegalArgumentException("Unexpected token");
        }

        Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId(); //재발급한토큰의 유저 ID 반환
        Users user = userService.findById(userId);   //유저 ID와 일치하는 유저 객체를 반환

        return tokenProvider.generateToken(user, Duration.ofHours(2));  //JWT 토큰 생성 이때 만료시간은 2시간
    }
}
