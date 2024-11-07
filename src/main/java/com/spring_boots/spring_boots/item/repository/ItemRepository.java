package com.spring_boots.spring_boots.item.repository;

import com.spring_boots.spring_boots.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
  // 카테고리 ID로 모든 아이템 조회
  List<Item> findAllByCategoryId(Long categoryId);

  // 카테고리 ID로 페이지네이션된 아이템 조회
  Page<Item> findAllByCategoryId(Long categoryId, Pageable pageable);


  // @Query("SELECT DISTINCT i FROM Item i JOIN i.keywords k WHERE LOWER(k) LIKE (CONCAT(:keyword, '%'))")
  // 키워드를 대소문자 구분없이 아이템 조회
  // 카테고리를 즉시 로딩하기 위한 FETCH JOIN 사용
  // 키워드, 상품명, 상품 브랜드명, 카테고리명, 테마명, 색상에 대해 LIKE 검색 수행 + 서브쿼리로 필요한 경우에만 색상 검색
  @Query("""
    SELECT DISTINCT i FROM Item i
    JOIN i.category c
    WHERE LOWER(i.itemName) LIKE (CONCAT('%', :keyword, '%'))
    OR LOWER(i.itemMaker) = :keyword
    OR LOWER(c.categoryName) = :keyword
    OR EXISTS (
        SELECT 1 FROM i.keywords k
        WHERE LOWER(k) LIKE (CONCAT(:keyword, '%'))
    )
    OR EXISTS (
        SELECT 1 FROM i.itemColor color
        WHERE LOWER(color) = :keyword
    )
  """)
  @EntityGraph(attributePaths = "category")
  Page<Item> findByIntegratedSearch(@Param("keyword") String keyword, Pageable pageable);

  // 카테고리 테마별로 아이템을 조회
  Page<Item> findByCategory_CategoryThema(String thema, Pageable pageable);

  // 상품 이름으로 조회
  Page<Item> findByItemNameContainingIgnoreCase(String itemName, Pageable pageable);
}
