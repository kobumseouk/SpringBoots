package com.spring_boots.spring_boots.common;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class UserRepositoryAspect {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryAspect.class);

    // 중복 조회 방지를 위한 캐시
    private final Map<String, Optional<?>> cache = new ConcurrentHashMap<>();

    // UserInfoRepository의 모든 메소드 호출 전 실행되는 Advice
    @Before("execution(* com.spring_boots.spring_boots.user.repository.UserInfoRepository.*(..))")
    public void logBeforeUserInfoRepositoryMethods(JoinPoint joinPoint) {
        logger.info("UserInfoRepository 메소드 호출: {}", joinPoint.getSignature().toShortString());
        logger.info("전달된 인자: {}", java.util.Arrays.toString(joinPoint.getArgs()));
    }

    // UserRepository의 모든 메소드 호출 전 실행되는 Advice
    @Before("execution(* com.spring_boots.spring_boots.user.repository.UserRepository.*(..))")
    public void logBeforeUserRepositoryMethods(JoinPoint joinPoint) {
        logger.info("UserRepository 메소드 호출: {}", joinPoint.getSignature().toShortString());
        logger.info("전달된 인자: {}", java.util.Arrays.toString(joinPoint.getArgs()));
    }


    // 중복 조회 방지를 위해 UserRepository의 findByUserRealId 메소드에 캐싱 적용
    @Around("execution(* com.spring_boots.spring_boots.user.repository.UserRepository.findByUserRealId(..))")
    public Object cacheFindByUserRealId(ProceedingJoinPoint joinPoint) throws Throwable {
        String userRealId = (String) joinPoint.getArgs()[0];

        // 캐시에 값이 있는지 확인
        if (cache.containsKey(userRealId)) {
            logger.info("캐시에서 값 반환: {}", userRealId);
            return cache.get(userRealId);
        }

        // 캐시에 값이 없으면 실제 메소드 호출 및 결과 캐싱
        Object result = joinPoint.proceed();
        cache.put(userRealId, (Optional<?>) result);
        logger.info("DB에서 조회한 값 캐시에 저장: {}", userRealId);
        return result;
    }


    // 중복 조회 방지를 위해 UserRepository의 findById 메소드에 캐싱 적용
    @Around("execution(* com.spring_boots.spring_boots.user.repository.UserRepository.findById(..))")
    public Object cacheFindById(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = (Long) joinPoint.getArgs()[0];
        String cacheKey = "findById:" + userId;

        if (cache.containsKey(cacheKey)) {
            logger.info("캐시에서 값 반환: {}", cacheKey);
            return cache.get(cacheKey);
        }

        Object result = joinPoint.proceed();
        cache.put(cacheKey, (Optional<?>) result);
        logger.info("DB에서 조회한 값 캐시에 저장: {}", cacheKey);
        return result;
    }

    // ItemRepository의 모든 메소드 호출 전 실행되는 Advice
    @Before("execution(* com.spring_boots.spring_boots.item.repository.ItemRepository.*(..))")
    public void logBeforeItemRepositoryMethods(JoinPoint joinPoint) {
        logger.info("ItemRepository 메소드 호출: {}", joinPoint.getSignature().toShortString());
        logger.info("전달된 인자: {}", java.util.Arrays.toString(joinPoint.getArgs()));
    }

    // CategoryRepository의 모든 메소드 호출 전 실행되는 Advice
    @Before("execution(* com.spring_boots.spring_boots.category.repository.CategoryRepository.*(..))")
    public void logBeforeCategoryRepositoryMethods(JoinPoint joinPoint) {
        logger.info("CategoryRepository 메소드 호출: {}", joinPoint.getSignature().toShortString());
        logger.info("전달된 인자: {}", java.util.Arrays.toString(joinPoint.getArgs()));
    }
}
