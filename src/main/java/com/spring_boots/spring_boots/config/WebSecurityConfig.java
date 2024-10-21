package com.spring_boots.spring_boots.config;

import com.spring_boots.spring_boots.config.jwt.JwtFilter;
import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import com.spring_boots.spring_boots.config.jwt.impl.UserDetailsServiceImpl;
import com.spring_boots.spring_boots.config.oauth.OAuth2SuccessHandler;
import com.spring_boots.spring_boots.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.spring_boots.spring_boots.user.repository.RefreshTokenRepository;
import com.spring_boots.spring_boots.user.service.OAuth2UserCustomService;
import com.spring_boots.spring_boots.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
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
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    //        private final TokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final JwtFilter jwtFilter;
//    private final UserDetailsServiceImpl userDetailsService;    //유저 정보를 인증하는 객체
    private final OAuth2SuccessHandler successHandler;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository cookieRepository;
//    private final RedisTemplate redisTemplate;    //Redis db 사용

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return (web) -> web.ignoring()
//                .requestMatchers(toH2Console())
//                .requestMatchers("/static/**");
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  // H2 콘솔 접근을 위해 CSRF 비활성화 todo 배포시 삭제
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))  // H2 콘솔에서 iframe 사용을 허용 todo 배포시 삭제
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(toH2Console()).permitAll()  // H2 콘솔에 대한 요청 허용
                        .requestMatchers(
                                "/api/**","/login/**","/static/**","/home/**",
                                "/","/register/**",
                                "/login-resource/**","api.js","elice-rabbit.png",
                                "useful-functions.js","elice-rabbit-favicon.png", "leaf.png",
                                "navbar.js", "/common/**","google.png"
                                //todo 배포시 api 에 대한 접근 권한 조정
                        ).permitAll()  // 모든 요청에 대해 요청 허가
                        .anyRequest().authenticated())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )// 세션 정책을 Stateless로 설정

                //###### OAuth2 로그인 설정 ########
                // OAuth2 로그인 설정
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/login")  // 커스텀 로그인 페이지 경로 설정
                        .authorizationEndpoint(authorizationEndpoint ->
                                authorizationEndpoint
                                        .authorizationRequestRepository(cookieRepository)  // 쿠키 기반 OAuth2 요청 저장소
                        )
                        .successHandler(successHandler)  // 로그인 성공 후 핸들러 설정
                        .userInfoEndpoint(userInfoEndpoint ->
                                userInfoEndpoint.userService(oAuth2UserCustomService)  // 사용자 정보 처리 서비스 설정
                        )
                )

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
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);

        return authenticationManagerBuilder.build();
    }

}
