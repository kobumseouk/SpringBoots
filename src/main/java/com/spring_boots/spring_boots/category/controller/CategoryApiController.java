package com.spring_boots.spring_boots.category.controller;

import com.spring_boots.spring_boots.category.dto.*;
import com.spring_boots.spring_boots.category.service.CategoryService;
import com.spring_boots.spring_boots.common.config.error.BadRequestException;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryApiController {

  private final CategoryService categoryService;

  // 관리자 - 새 카테고리 추가
  @PostMapping("/admin/categories")
  public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto requestDto) {
    try {
      CategoryResponseDto responseDto = categoryService.createCategory(requestDto);
      return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    } catch (BadRequestException e) {
      throw new BadRequestException("필수_파라미터_누락", "카테고리 생성 실패: " + e.getMessage());
    }
  }

  // 관리자 - 카테고리 정보 수정
  @PutMapping("/admin/categories/{category_id}")
  public ResponseEntity<CategoryResponseDto> updateCategory(
      @PathVariable("category_id") Long categoryId,
      @Valid @RequestBody CategoryRequestDto requestDto) {
    try {
      CategoryResponseDto responseDto = categoryService.updateCategory(categoryId, requestDto);
      return ResponseEntity.ok(responseDto);
    } catch (ResourceNotFoundException e) {
      throw new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId);
    } catch (BadRequestException e) {
      throw new BadRequestException("파라미터_길이_초과", "카테고리 수정 실패: " + e.getMessage());
    }
  }

  // 관리자 - 카테고리 삭제
  @DeleteMapping("/admin/categories/{category_id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable("category_id") Long categoryId) {
    try {
      categoryService.deleteCategory(categoryId);
      return ResponseEntity.noContent().build();
    } catch (ResourceNotFoundException e) {
      throw new ResourceNotFoundException("삭제할 카테고리를 찾을 수 없습니다: " + categoryId);
    }
  }

  // TODO: 테마에 대한 추가 정보가 필요할 경우 CategoryThemaDto로 변경 고려
  // 모든 카테고리 테마 목록 조회
  @GetMapping("/categories/themes")
  public ResponseEntity<List<String>> getAllThemes() {
    List<String> themes = categoryService.getAllThemes();
    return ResponseEntity.ok(themes);
  }

  // 특정 테마의 카테고리 목록 조회
  @GetMapping("/categories/themes/{category_thema}")
  public ResponseEntity<List<CategoryDto>> getCategoriesByThema(@PathVariable("category_thema") String thema) {
    List<CategoryDto> categories = categoryService.getCategoriesByThema(thema);
    return ResponseEntity.ok(categories);
  }

  // 카테고리 상세 조회
  @GetMapping("/categories/{category_id}")
  public ResponseEntity<CategoryDto> getCategoryDetail(@PathVariable("category_id") Long categoryId) {
    CategoryDto category = categoryService.getCategoryDetail(categoryId);
    return ResponseEntity.ok(category);
  }

  // 관리자 카테고리 전체 목록 조회 (페이지네이션)
  @GetMapping("/admin/categories")
  public ResponseEntity<List<CategoryAdminDto>> getAdminCategories(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "limit", defaultValue = "10") int limit) {
    try {
      List<CategoryAdminDto> listDto = categoryService.getAdminCategories(page, limit);
      return ResponseEntity.ok(listDto);
    } catch (BadRequestException e) {
      throw new BadRequestException("잘못된_파라미터_형식", "잘못된 페이지네이션 파라미터: " + e.getMessage());
    }
  }


}