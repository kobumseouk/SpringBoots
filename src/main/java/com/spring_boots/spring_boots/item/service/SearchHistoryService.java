package com.spring_boots.spring_boots.item.service;

import com.spring_boots.spring_boots.item.entity.SearchHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
  private final RedisTemplate<String, SearchHistory> searchLogRedisTemplate;
  private static final String KEY_PREFIX = "SearchLog";
  private static final long LIMIT = 5;

  // 검색 기록 저장
  // key: SearchLog + userId, value: 검색어, 검색일자
  public void saveSearchKeyword(Long userId, String keyword) {
    String key = KEY_PREFIX + userId;
    SearchHistory searchHistory = new SearchHistory(keyword);

    // Redis List 작업을 위한 Operations 객체
    ListOperations<String, SearchHistory> listOps = searchLogRedisTemplate.opsForList();

    // 중복 검색어 제거
    listOps.remove(key, 0, searchHistory);

    // 새 검색어 추가 (왼쪽에서부터)
    listOps.leftPush(key, searchHistory);

    // 최근 5개만 유지
    listOps.trim(key, 0, LIMIT - 1);
  }

  // 최근 검색어 조회 (limit)
  public List<String> getRecentSearches(Long userId) {
    String key = KEY_PREFIX + userId;
    ListOperations<String, SearchHistory> listOps = searchLogRedisTemplate.opsForList();

    // 전체 리스트 가져오기
    List<SearchHistory> histories = listOps.range(key, 0, -1);

    return histories.stream()
        .map(SearchHistory::getKeyword)
        .collect(Collectors.toList());
  }

  // 개별 검색어 삭제
  public void deleteSearchHistory(Long userId, String keyword) {
    String key = KEY_PREFIX + userId;
    ListOperations<String, SearchHistory> listOps = searchLogRedisTemplate.opsForList();

    SearchHistory target = new SearchHistory(keyword);
    listOps.remove(key, 0, target);
  }
}
