package com.spring_boots.spring_boots.item.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@RedisHash(value = "SearchHistory", timeToLive = 60 * 60 * 24 * 7)
@Getter
@Setter
@NoArgsConstructor
public class SearchHistory {

  private String keyword;
  private LocalDateTime searchedAt;

  public SearchHistory(String keyword) {
    this.keyword = keyword;
    this.searchedAt = LocalDateTime.now();
  }
}
