package com.spring_boots.spring_boots.config.jwt.impl;

import com.spring_boots.spring_boots.config.jwt.JwtProvider;
import com.spring_boots.spring_boots.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static com.spring_boots.spring_boots.config.UserConstants.ACCESS_TOKEN_TYPE_VALUE;
import static com.spring_boots.spring_boots.config.UserConstants.REFRESH_TOKEN_TYPE_VALUE;
import static com.spring_boots.spring_boots.config.jwt.AuthToken.AUTHORITIES_TOKEN_KEY;

@Component
public class JwtProviderImpl implements JwtProvider<AuthTokenImpl> {
    @Value("${jwt.secret}") //설정 정보 파일에 값을 가져옴.
    private String secret;

    @Value("${jwt.token.access-expires}")
    private long accessExpires;

    @Value("${jwt.token.refresh-expires}")
    private long refreshExpires;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public AuthTokenImpl convertAuthToken(String token) {
        return new AuthTokenImpl(token, key);
    }

    @Override
    public Authentication getAuthentication(AuthTokenImpl authToken) {
        if (authToken.validate()) {
            Claims claims = authToken.getDate();

            if (!claims.get("type").equals(ACCESS_TOKEN_TYPE_VALUE)) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid token type"
                );
            }

            Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                    new SimpleGrantedAuthority(claims.get(
                            AUTHORITIES_TOKEN_KEY,
                            String.class)
                    ));

            User principal =
                    new User(claims.getSubject(), "", authorities);

            return new UsernamePasswordAuthenticationToken(
                    principal,
                    authToken,
                    authorities
            );
        } else {
            throw new JwtException("token Error");
        }
    }

    @Override
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
                new Date(System.currentTimeMillis() + accessExpires)
        );
    }

    @Override
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
                new Date(System.currentTimeMillis() + refreshExpires)
        );
    }
}
