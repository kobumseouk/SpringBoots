package com.spring_boots.spring_boots.category.repository;

import com.spring_boots.spring_boots.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, Long> {

  // 특정 테마에 속한 카테고리들을 displayOrder 순으로 조회
  List<Category> findByCategoryThemaOrderByDisplayOrder(String categoryThema);

  // DisplayOrder가 1이 아닌 카테고리 조회 메서드
  List<Category> findByDisplayOrderNot(int displayOrder);

  // 모든 고유한 테마 목록 조회
  @Query("SELECT DISTINCT c.categoryThema FROM Category c")
  List<String> findDistinctThemas();

  // int countByCategoryThema(String thema);


  // 카테고리 생성 시 배치 순서가 같거나 큰 기존의 카테고리의 배치는 + 1
  @Modifying
  @Query("UPDATE Category c SET c.displayOrder = c.displayOrder + 1 " +
      "WHERE c.categoryThema = :categoryThema AND c.displayOrder >= :order")
  void incrementDisplayOrderForSubsequentCategories(@Param("categoryThema") String categoryThema, @Param("order") int order);
  // 카테고리 수정 시 수정한 배치 순서보다 작은 기존의 카테고리의 배치는 - 1
  @Modifying
  @Query("UPDATE Category c SET c.displayOrder = c.displayOrder - 1 " +
      "WHERE c.categoryThema = :categoryThema AND c.displayOrder > :start AND c.displayOrder <= :end")
  void decrementDisplayOrderForIntermediateCategories(@Param("categoryThema") String categoryThema, @Param("start") int start, @Param("end") int end);
  // 카테고리 수정 시 수정한 배치 순서와 동일하거나 큰 기존의 카테고리의 배치는 + 1
  @Modifying
  @Query("UPDATE Category c SET c.displayOrder = c.displayOrder + 1 " +
      "WHERE c.categoryThema = :categoryThema AND c.displayOrder >= :start AND c.displayOrder < :end")
  void incrementDisplayOrderForIntermediateCategories(@Param("categoryThema") String categoryThema, @Param("start") int start, @Param("end") int end);

  // 카테고리 테마에 속한 카테고리 수 반환
  int countByCategoryThema(String categoryThema);

}
