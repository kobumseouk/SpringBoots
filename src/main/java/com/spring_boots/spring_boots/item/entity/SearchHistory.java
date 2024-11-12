package com.spring_boots.spring_boots.item.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@RedisHash(value = "SearchHistory", timeToLive = 60 * 60 * 24 * 7)
@Getter
@Setter
@NoArgsConstructor
public class SearchHistory {

  private String keyword;
  private String searchedAt;

  public SearchHistory(String keyword) {
    this.keyword = keyword;
    this.searchedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  }

  // 중복 확인
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SearchHistory that = (SearchHistory) o;
    return Objects.equals(keyword, that.keyword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyword);
  }
}
