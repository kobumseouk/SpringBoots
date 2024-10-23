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
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

/**
 * Security 커스텀설정 정보
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final JwtFilter jwtFilter;
    private final OAuth2SuccessHandler successHandler;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository cookieRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  // H2 콘솔 접근을 위해 CSRF 비활성화, 배포시 주석처리
//                .csrf(csrf -> csrf.ignoringRequestMatchers(
//                        "/login","/register","/logout",
//                        "/login/**","/register/**",
//                        "/api/login","/api/logout"
//                ))
//                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))  // H2 콘솔에서 iframe 사용을 허용, 배포시 주석처리
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(toH2Console()).permitAll()  // H2 콘솔에 대한 요청 허용, 배포시 주석처리
                        .requestMatchers(
                                //Api
                                "/api/**",
                                //정적경로
                                "/category-detail/**","/category-form/**", "/category-recommend/**",    //카테고리
                                "/common/**","/search/**",   //공통헤더
                                "/event-detail/**","/event-form/**","/event-list/**",   //이벤트
                                "/home/**","/images/**","/login/**","/register/**", //로그인, 홈, 회원가입
                                "/product-detail/**","/page-not-found/**",  //상품
                                "api.js", "useful-functions.js",    //공통 js 파일
                                "indexed-db.js", "navbar.js",   //공통 js 파일
                                "elice-rabbit.png","elice-rabbit-favicon.png",  //이미지
                                "leaf.png","google.png",    //이미지
                                //url 경로
                                "/","/login","/register",
                                "/categories/**","/items/**","/events/**"
                        ).permitAll()  // 모든 요청에 대해 요청 허가
                        .anyRequest().authenticated())
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("https://xsadxbhpffmfhfsx.tunnel-pt.elice.io/", "http://localhost:3000")); // 허용할 도메인
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH")); // 허용할 메서드
                    config.setAllowCredentials(true); // 인증 정보 포함 여부
                    config.setAllowedHeaders(List.of("*")); // 허용할 헤더
                    return config;
                }))

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
