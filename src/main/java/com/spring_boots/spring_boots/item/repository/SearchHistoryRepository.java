package com.spring_boots.spring_boots.item.repository;

import com.spring_boots.spring_boots.item.entity.SearchHistory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SearchHistoryRepository extends CrudRepository<SearchHistory, String> {
  List<SearchHistory> findByUserIdOrderBySearchedAtDesc(Long userId);

  // 유저의 검색기록 개별 삭제
  void deleteByUserIdAndKeyword(Long userId, String keyword);
  // 유저의 검색기록 전체 삭제
  void deleteByUserId(Long userId);
}
