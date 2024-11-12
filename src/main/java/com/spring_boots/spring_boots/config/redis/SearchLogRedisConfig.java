package com.spring_boots.spring_boots.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spring_boots.spring_boots.item.entity.SearchHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@EnableCaching
@Configuration
@RequiredArgsConstructor
public class SearchLogRedisConfig {

  @Bean
  public RedisTemplate<String, SearchHistory> searchLogRedisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, SearchHistory> searchTemplate = new RedisTemplate<>();
    searchTemplate.setConnectionFactory(connectionFactory);

    // ObjectMapper 설정
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    // JSON 직렬화를 위한 설정
    Jackson2JsonRedisSerializer<SearchHistory> jsonSerializer =
        new Jackson2JsonRedisSerializer<>(SearchHistory.class);
    jsonSerializer.setObjectMapper(objectMapper);

    searchTemplate.setKeySerializer(new StringRedisSerializer());
    searchTemplate.setValueSerializer(jsonSerializer);
    searchTemplate.setHashKeySerializer(new StringRedisSerializer());
    searchTemplate.setHashValueSerializer(jsonSerializer);

    return searchTemplate;
  }


}
