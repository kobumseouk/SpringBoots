package com.spring_boots.spring_boots.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * url 패스 경로 바꾸기
 * addViewController("원하는 경로").setViewName("forward:현재 정적 경로")
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // /login 으로 요청이 들어오면 login/login.html로 매핑
        registry.addViewController("/login").setViewName("forward:/login/login.html");
        //forward 는 서버 내에서 요청을 리다이렉트하지 않고 다른 경로로 넘겨주는 방식, 내부적으로 처리됨.
        registry.addViewController("/order-summary").setViewName("forward:/order-summary/order-summary.html");
        registry.addViewController("/order-list").setViewName("forward:/order-list/order-list.html");
        registry.addViewController("/order").setViewName("forward:/order/order.html");
        registry.addViewController("/order-details").setViewName("forward:/order-details/order-details.html");
        // 카테고리 목록 페이지
        registry.addViewController("/categories").setViewName("forward:/category/list.html");
        // 카테고리 상세 페이지
        // registry.addViewController("/category/**").setViewName("forward:/category/detail.html");
        // 테마별 카테고리 목록
        registry.addViewController("/categories/themas").setViewName("forward:/category/thema-list.html");
        // 관리자 카테고리 관리 페이지
        registry.addViewController("/admin/categories").setViewName("forward:/admin/category-management.html");
        // 카테고리 생성 페이지
        registry.addViewController("/admin/categories/create").setViewName("forward:/admin/category-create.html");

        // 이벤트 목록 페이지
        registry.addViewController("/events").setViewName("forward:/event/list.html");
        // 이벤트 생성 페이지
        registry.addViewController("/events/create").setViewName("forward:/event-create.html");


    }
}
