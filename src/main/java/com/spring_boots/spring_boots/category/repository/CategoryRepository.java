package com.spring_boots.spring_boots.category.repository;

import com.spring_boots.spring_boots.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, Long> {

  // 특정 테마에 속한 카테고리들을 displayOrder 순으로 조회
  List<Category> findByCategoryThemaOrderByDisplayOrder(String categoryThema);

  // 모든 고유한 테마 목록 조회
  @Query("SELECT DISTINCT c.categoryThema FROM Category c")
  List<String> findDistinctThemas();

  // List<Category> findByThema(String thema);

  // 카테고리 이름으로 카테고리 조회 : 중복 확인
  // Category findByCategoryName(String categoryName);

}
