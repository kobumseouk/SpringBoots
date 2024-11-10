package com.spring_boots.spring_boots.item.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@RedisHash("SearchHistory")
@Getter
@Setter
public class SearchHistory {
  @Id
  private String id;  // userId:keyword 형태로 저장
  private Long userId;
  private String keyword;
  private LocalDateTime searchedAt;


}
