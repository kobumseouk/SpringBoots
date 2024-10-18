package com.spring_boots.spring_boots.item.repository;

import com.spring_boots.spring_boots.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
  // 카테고리 ID로 모든 아이템 찾기
  List<Item> findAllByCategoryId(Long categoryId);

  // 카테고리 ID로 페이지네이션된 아이템 찾기
  Page<Item> findAllByCategoryId(Long categoryId, Pageable pageable);

  // 키워드를 대소문자 구분없이 포함하면 페이지로 검색
  Page<Item> findByItemNameContainingIgnoreCaseOrKeywordsContainingIgnoreCase(String keyword, Pageable pageable);
}
