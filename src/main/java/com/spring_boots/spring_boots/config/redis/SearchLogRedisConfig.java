package com.spring_boots.spring_boots.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@EnableCaching
@Configuration
@RequiredArgsConstructor
public class SearchLogRedisConfig {

  @Bean
  public RedisTemplate<String, String> searchLogRedisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> searchTemplate = new RedisTemplate<>();
    searchTemplate.setConnectionFactory(connectionFactory);

    // 문자열 직렬화 설정
    searchTemplate.setKeySerializer(new StringRedisSerializer());
    searchTemplate.setValueSerializer(new StringRedisSerializer());
    searchTemplate.setHashKeySerializer(new StringRedisSerializer());
    searchTemplate.setHashValueSerializer(new StringRedisSerializer());

    return searchTemplate;
  }


}
