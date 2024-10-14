package com.spring_boots.spring_boots.category.controller;


import com.spring_boots.spring_boots.category.dto.category.CategoryAdminDto;
import com.spring_boots.spring_boots.category.dto.category.CategoryRequestDto;
import com.spring_boots.spring_boots.category.dto.category.CategoryResponseDto;
import com.spring_boots.spring_boots.category.service.CategoryService;
import com.spring_boots.spring_boots.common.config.error.BadRequestException;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryApiController {

  private final CategoryService categoryService;

  // 관리자 - 새 카테고리 추가
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto requestDto) {
    CategoryResponseDto responseDto = categoryService.createCategory(requestDto);
    return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
  }

  // 관리자 - 카테고리 정보 수정
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{category_id}")
  public ResponseEntity<CategoryResponseDto> updateCategory(
      @PathVariable("category_id") Long categoryId,
      @Valid @RequestBody CategoryRequestDto requestDto) {
    CategoryResponseDto responseDto = categoryService.updateCategory(categoryId, requestDto);
    return ResponseEntity.ok(responseDto);
  }

  // 관리자 - 카테고리 삭제
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{category_id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable("category_id") Long categoryId) {
    categoryService.deleteCategory(categoryId);
    return ResponseEntity.noContent().build();
  }

  // 관리자 카테고리 전체 목록 조회 (페이지네이션)
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<Page<CategoryAdminDto>> getAdminCategories(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "limit", defaultValue = "10") int limit) {
    Page<CategoryAdminDto> pageDto = categoryService.getAdminCategories(page, limit);
    return ResponseEntity.ok(pageDto);
  }

  // 관리자 개별 카테고리 조회 - 카테고리 수정 시 필요
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{category_id}")
  public ResponseEntity<CategoryAdminDto> getAdminCategory(@PathVariable("category_id") Long categoryId) {
    CategoryAdminDto categoryDto = categoryService.getAdminCategory(categoryId);
    return ResponseEntity.ok(categoryDto);
  }

}
