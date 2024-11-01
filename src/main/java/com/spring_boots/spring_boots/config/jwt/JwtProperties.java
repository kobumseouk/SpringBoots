package com.spring_boots.spring_boots.config.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * jwt 설정정보
 * */
@Configuration
@Getter
@Setter
public class JwtProperties {

    private final String secret;

    private final long accessExpires;

    private final long refreshExpires;

    public JwtProperties(@Value("${jwt.secret}") String secret,
                         @Value("${jwt.token.access-expires}") long accessExpires,
                         @Value("${jwt.token.refresh-expires}") long refreshExpires) {
        this.secret = secret;
        this.accessExpires = accessExpires;
        this.refreshExpires = refreshExpires;
    }
}
