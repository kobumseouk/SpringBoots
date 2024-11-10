package com.spring_boots.spring_boots.item.repository;

import com.spring_boots.spring_boots.item.entity.SearchHistory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SearchHistoryRepository extends CrudRepository<SearchHistory, String> {
  List<SearchHistory> findByUserIdOrderBySearchedAtDesc(Long userId);
  void deleteByUserIdAndKeyword(Long userId, String keyword);
}
