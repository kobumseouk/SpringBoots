package com.spring_boots.spring_boots.user.service;

import com.spring_boots.spring_boots.config.jwt.impl.AuthTokenImpl;
import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import com.spring_boots.spring_boots.user.domain.TokenRedis;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.dto.request.JwtTokenDto;
import com.spring_boots.spring_boots.user.dto.request.JwtTokenLoginRequest;
import com.spring_boots.spring_boots.user.exception.PasswordNotMatchException;
import com.spring_boots.spring_boots.user.exception.UserDeletedException;
import com.spring_boots.spring_boots.user.exception.UserNotFoundException;
import com.spring_boots.spring_boots.user.repository.TokenRedisRepository;
import com.spring_boots.spring_boots.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtProviderImpl jwtProvider;
    private final TokenRedisRepository tokenRedisRepository;

    public JwtTokenDto login(JwtTokenLoginRequest request) {
        Users user = userRepository.findByUserRealId(request.getUserRealId())
                .orElseThrow(() -> new UserNotFoundException("가입되지 않은 ID 입니다."));

        if (user.isDeleted()) {
            throw new UserDeletedException("회원 정보가 삭제된 상태입니다.");
        }

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new PasswordNotMatchException("잘못된 비밀번호입니다.");
        }

        Map<String, Object> claims = Map.of(
                "accountId", user.getUserId(),  //JWT 클래임에 accountId
                "role", user.getRole(),  //JWT 클래임에 role
                "provider", user.getProvider(),
                "userRealId", user.getUserRealId()   //JWT 클래임에 실제 ID 추가
        );

        AuthTokenImpl accessToken = jwtProvider.createAccessToken(
                user.getUserRealId(),   //토큰에 실제 ID 정보 입력
                user.getRole(),
                claims
        );

        AuthTokenImpl refreshToken = jwtProvider.createRefreshToken(
                user.getUserRealId(),   //토큰에 실제 ID 정보 입력
                user.getRole(),
                claims
        );

        //리프레시 토큰은 redis 에 저장
        tokenRedisRepository.save(
                new TokenRedis(user.getUserRealId(), refreshToken.getToken()));

        return JwtTokenDto.builder()
                .accessToken(accessToken.getToken())
                .role(user.getRole())
                .build();
    }

    public boolean validateToken(String accessToken) {
        return jwtProvider.validateToken(accessToken);
    }
}
