package com.spring_boots.spring_boots.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryService {
  private final RedisTemplate<String, String> searchLogRedisTemplate;  // SearchHistory -> String
  private static final String KEY_PREFIX = "SearchLog";
  private static final long LIMIT = 5;

  // 검색 기록 저장 - 파이프라인 적용
  public void saveSearchKeyword(Long userId, String keyword) {
    try {
      String key = KEY_PREFIX + userId;

      // Redis List 작업을 위한 Operations 객체
      ListOperations<String, String> listOps = searchLogRedisTemplate.opsForList();

      // 중복 검색어 제거
      listOps.remove(key, 0, keyword);  // 0 : 모든 일치 항목 -> 중복 제거 후 추가

      // 새 검색어 추가 (왼쪽에서부터)
      listOps.leftPush(key, keyword);

      // 최근 5개만 유지
      listOps.trim(key, 0, LIMIT - 1);
    } catch (Exception e) {
      log.error("검색어 저장 중 오류 발생: ", e);
    }
  }

  // 최근 검색어 조회 (limit)
  public List<String> getRecentSearches(Long userId) {
    try {
      String key = KEY_PREFIX + userId;
      ListOperations<String, String> listOps = searchLogRedisTemplate.opsForList();

      // 전체 리스트 가져오기
      List<String> histories = listOps.range(key, 0, -1);   // -1 : 인덱스 끝까지 지정

      return Optional.ofNullable(histories).orElse(new ArrayList<>());
    } catch (Exception e) {
      log.error("검색어 조회 중 오류 발생: ", e);
      return new ArrayList<>();
    }
  }

  // 개별 검색어 삭제
  public void deleteSearchHistory(Long userId, String keyword) {
    try {
      String key = KEY_PREFIX + userId;
      ListOperations<String, String> listOps = searchLogRedisTemplate.opsForList();
      listOps.remove(key, 0, keyword);  // 0 : 모든 일치 항목
    } catch (Exception e) {
      log.error("검색어 삭제 중 오류 발생: ", e);
    }
  }

  // 검색 기록 전체 삭제
  public void deleteAllSearchHistory(Long userId) {
    try {
      String key = KEY_PREFIX + userId;
      searchLogRedisTemplate.delete(key);  // 해당 키의 모든 데이터 삭제
    } catch (Exception e) {
      log.error("검색 기록 전체 삭제 중 오류 발생: ", e);
    }
  }

}

