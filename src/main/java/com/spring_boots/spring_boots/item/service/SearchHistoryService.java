package com.spring_boots.spring_boots.item.service;

import com.spring_boots.spring_boots.item.entity.SearchHistory;
import com.spring_boots.spring_boots.item.repository.SearchHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
  private final SearchHistoryRepository searchHistoryRepository;

  public void saveSearchKeyword(Long userId, String keyword) {
    // 중복 검색어 제거
    searchHistoryRepository.deleteByUserIdAndKeyword(userId, keyword);

    // 새로운 검색 기록 저장
    SearchHistory searchHistory = new SearchHistory();
    searchHistory.setId(userId + ":" + keyword);
    searchHistory.setUserId(userId);
    searchHistory.setKeyword(keyword);
    searchHistory.setSearchedAt(LocalDateTime.now());

    searchHistoryRepository.save(searchHistory);

    // 최근 10개만 유지
    List<SearchHistory> histories = searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId);
    if (histories.size() > 5) {
      for (int i = 5; i < histories.size(); i++) {
        searchHistoryRepository.delete(histories.get(i));
      }
    }
  }

  // 최근 검색기록 정렬 조회
  public List<String> getRecentSearches(Long userId) {
    return searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId)
        .stream()
        .map(SearchHistory::getKeyword)
        .collect(Collectors.toList());
  }

  // 개별 검색어 삭제
  @Transactional
  public void deleteSearchHistory(Long userId, String keyword) {
    searchHistoryRepository.deleteByUserIdAndKeyword(userId, keyword);
  }


}
