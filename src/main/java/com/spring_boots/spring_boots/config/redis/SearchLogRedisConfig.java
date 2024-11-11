package com.spring_boots.spring_boots.config.redis;

import com.spring_boots.spring_boots.item.entity.SearchHistory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class SearchLogRedisConfig {
  @Bean
  public RedisTemplate<String, SearchHistory> searchLogRedisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, SearchHistory> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // JSON 직렬화를 위한 설정
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new Jackson2JsonRedisSerializer<>(SearchHistory.class));

    return template;
  }
}
