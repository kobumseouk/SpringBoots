package com.spring_boots.spring_boots.config;

import com.spring_boots.spring_boots.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Security Config 파일
 * 순환참조 발생하는 빈 객체는 따로 분리
 */
@Configuration
@RequiredArgsConstructor
public class SecurityBeanConfig {

    //순환참조 발생하여 따로 분리
    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
