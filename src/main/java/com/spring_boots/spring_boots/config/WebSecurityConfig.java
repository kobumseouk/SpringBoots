package com.spring_boots.spring_boots.config;

import com.spring_boots.spring_boots.config.jwt.JwtFilter;
import com.spring_boots.spring_boots.config.jwt.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

/**
 * Security 커스텀설정 정보
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

//    private final TokenProvider tokenProvider;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final JwtFilter jwtFilter;
    private final UserDetailsServiceImpl userService;
//    private final RedisTemplate redisTemplate;    //Redis db 사용

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(toH2Console())
                .requestMatchers("/static/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  // H2 콘솔 접근을 위해 CSRF 비활성화
                .headers(headers -> headers.frameOptions().disable())  // H2 콘솔에서 iframe 사용을 허용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(toH2Console()).permitAll()  // H2 콘솔에 대한 요청 허용
                        .requestMatchers(
                                "/api/**", "/login/**"
                        ).permitAll()  // 모든 요청에 대해 요청 허가
                        .anyRequest().permitAll())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )// 세션 정책을 Stateless로 설정

                // ######### 폼 기반 로그인 설정 #######
                .formLogin(form -> form
                        .loginPage("/login")    //로그인 페이지 경로 설정
                        .defaultSuccessUrl("/home") //로그인 성공페이지
                        .successHandler(successHandler)
                        .permitAll()    //로그인페이지에 대한 모든 요청 허용
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login") //로그아웃 성공 페이지
                        .invalidateHttpSession(true)    //세션 무효화
                        .permitAll()    //로그아웃 요청 접근 허용
                )
                //###################

                //###### OAuth2 로그인 설정 ########
//                .oauth2Login(oauth2Login->
//                        oauth2Login
//                                .defaultSuccessUrl("로그인 성공 페이지",true))
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class) //JWT 필터추가
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            BCryptPasswordEncoder bCryptPasswordEncoder
    ) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder
                = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userService)
                .passwordEncoder(bCryptPasswordEncoder);

        return authenticationManagerBuilder.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
