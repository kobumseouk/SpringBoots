package com.spring_boots.spring_boots.category.controller;

import com.spring_boots.spring_boots.category.dto.category.CategoryDto;
import com.spring_boots.spring_boots.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryApiController {

  private final CategoryService categoryService;


  // TODO: 테마에 대한 추가 정보가 필요할 경우 CategoryThemaDto로 변경 고려
  // 모든 카테고리 테마 목록 조회
  @GetMapping("/themas")
  public ResponseEntity<List<String>> getAllThemas() {
    List<String> themas = categoryService.getAllThemas();
    return ResponseEntity.ok(themas);
  }

  // 특정 테마의 카테고리 목록 조회 -> HOW TO 와 같은 형식에서 사용
  @GetMapping("/themas/{category_thema}")
  public ResponseEntity<List<CategoryDto>> getCategoriesByThema(@PathVariable("category_thema") String thema) {
    List<CategoryDto> categories = categoryService.getCategoriesByThema(thema);
    return ResponseEntity.ok(categories);
  }

  // 카테고리 상세 조회 -> 공용, 여성, 남성, 액세서리, SALE 카테고리 선택 시 사용(기본값: 전체보기)
  @GetMapping("/{category_id}")
  public ResponseEntity<CategoryDto> getCategoryDetail(@PathVariable("category_id") Long categoryId) {
    CategoryDto category = categoryService.getCategoryDetail(categoryId);
    return ResponseEntity.ok(category);
  }
  
}