package com.spring_boots.spring_boots.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return (web) -> web.ignoring()
//                .requestMatchers(toH2Console())
//                .requestMatchers("/static/**");
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  // H2 콘솔 접근을 위해 CSRF 비활성화
                .headers(headers -> headers.frameOptions().disable())  // H2 콘솔에서 iframe 사용을 허용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(toH2Console()).permitAll()  // H2 콘솔에 대한 요청 허용
                        .anyRequest().permitAll())  // 모든 요청에 대해 요청 허가
                .build();
    }


}
