package com.spring_boots.spring_boots.item.repository;

import com.spring_boots.spring_boots.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
  List<Item> findAllByCategoryId(Long categoryId);

  // 키워드를 대소문자 구분없이 포함하면 페이지로 검색
  Page<Item> findByKeywordsContainingIgnoreCase(String keyword, Pageable pageable);
}
