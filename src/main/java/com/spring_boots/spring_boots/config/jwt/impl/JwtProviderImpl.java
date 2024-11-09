package com.spring_boots.spring_boots.config.jwt.impl;

import com.spring_boots.spring_boots.config.jwt.JwtProperties;
import com.spring_boots.spring_boots.user.domain.TokenRedis;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.exception.TokenNotFoundException;
import com.spring_boots.spring_boots.user.repository.TokenRedisRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import static com.spring_boots.spring_boots.config.jwt.UserConstants.*;

/**
 * 토큰과 관련된 로직
 * */

@Component
@RequiredArgsConstructor
public class JwtProviderImpl{
    private final JwtProperties jwtProperties;
    private Key key;

    private final UserDetailsServiceImpl userDetailsService;
    private final TokenRedisRepository tokenRedisRepository;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Authentication getAuthentication(String authToken) {
        String username = extractUsername(authToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public AuthTokenImpl createAccessToken(
            String userId,
            UserRole role,
            Map<String, Object> claimsMap
    ) {
        Claims claims = new DefaultClaims(claimsMap); // Map을 Claims로 변환
        claims.put("type", ACCESS_TOKEN_TYPE_VALUE);
        return new AuthTokenImpl(
                userId,
                role,
                key,
                (Claims) claims,
                new Date(System.currentTimeMillis() + jwtProperties.getAccessExpires())
        );
    }

    public AuthTokenImpl createRefreshToken(
            String userId,
            UserRole role,
            Map<String, Object> claimsMap
    ) {
        Claims claims = new DefaultClaims(claimsMap); // Map을 Claims로 변환
        claims.put("type", REFRESH_TOKEN_TYPE_VALUE);
        return new AuthTokenImpl(
                userId,
                role,
                key,
                (Claims) claims,
                new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpires())
        );
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //토큰 정보를 비밀키를 이용해 해석
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperties.getSecret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // JWT가 만료된 경우에도 Claims를 반환할 수 있도록 예외에서 Claims를 가져옴
            return e.getClaims();  // 만료된 토큰에서 Claims 정보를 가져옴
        }
    }


    //토큰 만료여부에 따라 true or false 반환
    public boolean validateToken(String jwtToken) {
        return !isTokenExpired(jwtToken);
    }

    //토큰 만료여부
    private boolean isTokenExpired(String jwtToken) {
        return extractExpiration(jwtToken).before(new Date());
    }

    //날짜 추출
    private Date extractExpiration(String jwtToken) {
        return extractAllClaims(jwtToken).getExpiration();
    }

    // 리프레시 토큰을 사용하여 새로운 액세스 토큰 생성
    //사용자가 리프레시토큰을 가지고 있을 경우
    public String generateAccessTokenFromRefreshToken(String refreshToken) {
        // 리프레시 토큰 검증
        if (!validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 리프레시 토큰에서 사용자 정보를 추출
        Claims claims = extractAllClaims(refreshToken); //리프레시토큰에 있는 모든 정보를 추출
        String userId = claims.getSubject(); //리프레시토큰에 있는 사용자 ID 또는 고유 식별자
        String userRealId = claims.get("userRealId", String.class); // 사용자 실제 ID
        Long accountId = claims.get("accountId", Long.class); // 계정 ID
        String role = claims.get("role", String.class); // 사용자 역할

        // 새로운 액세스 토큰 생성
        return Jwts.builder()
                .setSubject(userId) // 사용자 ID 설정
                .claim("userRealId", userRealId) // 실제 사용자 ID 설정
                .claim("accountId", accountId) // 계정 ID 설정
                .claim("role", role) // 사용자 역할 설정
                .claim("type", "access_token") // 토큰 타입 설정
                .setIssuedAt(new Date(System.currentTimeMillis())) // 발급 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessExpires())) // 만료 시간 설정
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret()) // 서명 알고리즘 및 비밀키 설정
                .compact(); // JWT 생성
    }

    public boolean validateAdminToken(String accessToken) {
        try {
            // JWT에서 Claims(토큰에 포함된 정보) 파싱
            Claims claims = extractAllClaims(accessToken);

            // Claims에서 사용자 역할 정보 추출 (예: "ROLE_ADMIN"인지 확인)
            String role = claims.get("role", String.class);

            // 사용자 역할이 관리자("ROLE_ADMIN")이면 true 반환
            return "ADMIN".equals(role);
        } catch (SignatureException e) {
            // 서명이 유효하지 않은 경우 (토큰이 변조되었을 때)
            throw new RuntimeException("Invalid JWT signature");
        } catch (Exception e) {
            // 그 외의 오류 (만료된 토큰 등)
            throw new RuntimeException("Token validation error: " + e.getMessage());
        }
    }

    //redis 에 리프레시토큰 정보를 가져와 처리
    public String generateAccessTokenFromRefreshTokenByRedis(String jwtAccessToken) {
        // 리프레시 토큰에서 사용자 정보를 추출
        Claims claims = extractAllClaims(jwtAccessToken); //리프레시토큰에 있는 모든 정보를 추출
        String userRealId = claims.get("userRealId", String.class); // 사용자 실제 ID

        //리프레시토큰이 만료됐을 경우 null 로 처리해서 return
        TokenRedis tokenInfo = tokenRedisRepository.findById(userRealId)
                .orElse(null);

        if (tokenInfo == null) {
            return NOT_FOUND_REFRESH_TOKEN;
        }

        Claims claimsByRedis = extractAllClaims(tokenInfo.getRefreshToken());
        String userIdByRedis = claimsByRedis.getSubject(); //리프레시토큰에 있는 사용자 ID 또는 고유 식별자
        String userRealIdByRedis = claimsByRedis.get("userRealId", String.class); // 사용자 실제 ID
        Long accountIdByRedis = claimsByRedis.get("accountId", Long.class); // 계정 ID
        String roleByRedis = claimsByRedis.get("role", String.class); // 사용자 역할

        // 새로운 액세스 토큰 생성
        return Jwts.builder()
                .setSubject(userIdByRedis) // 사용자 ID 설정
                .claim("userRealId", userRealIdByRedis) // 실제 사용자 ID 설정
                .claim("accountId", accountIdByRedis) // 계정 ID 설정
                .claim("role", roleByRedis) // 사용자 역할 설정
                .claim("type", "access_token") // 토큰 타입 설정
                .setIssuedAt(new Date(System.currentTimeMillis())) // 발급 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessExpires())) // 만료 시간 설정
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret()) // 서명 알고리즘 및 비밀키 설정
                .compact(); // JWT 생성
    }
}
