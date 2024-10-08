package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.category.*;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;


  // 새 카테고리 생성
  @PreAuthorize("hasRole('ADMIN')")  // 컨트롤러 선에서 작동 중이라 생략 or 2차 보안
  @Transactional
  public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
    Category category = categoryMapper.categoryRequestDtoToCategory(requestDto);

    return categoryMapper.categoryToCategoryResponseDto(categoryRepository.save(category));
  }


  // 카테고리 수정
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public CategoryResponseDto updateCategory(Long categoryId, CategoryRequestDto requestDto) {
    Category category = categoryRepository.findById(categoryId)
        .map(existingCategory -> {
          categoryMapper.updateCategoryFromDto(requestDto, existingCategory);
          return categoryRepository.save(existingCategory);
        })
        .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));

    return categoryMapper.categoryToCategoryResponseDto(category);
  }


  // 카테고리 삭제
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public void deleteCategory(Long categoryId) {
    categoryRepository.findById(categoryId)
        .ifPresentOrElse(
            categoryRepository::delete,
            () -> { throw new ResourceNotFoundException("삭제할 카테고리를 찾을 수 없습니다: " + categoryId); }
        );
  }


  // 카테고리 전체 테마 목록 조회
  public List<String> getAllThemas() {
    return categoryRepository.findDistinctThemas();
  }

  // 특정 테마의 카테고리 목록 조회
  public List<CategoryDto> getCategoriesByThema(String thema) {
    List<Category> categories = categoryRepository.findByCategoryThemaOrderByDisplayOrder(thema);
    return categories.stream()
        .map(categoryMapper::categoryToCategoryDto)
        .collect(Collectors.toList());
  }

  // 카테고리 상세 조회
  public CategoryDto getCategoryDetail(Long categoryId) {
    return categoryRepository.findById(categoryId)
        .map(categoryMapper::categoryToCategoryDto)
        .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));
  }


  // 관리자용 카테고리 목록 페이지네이션 적용하여 조회
  @PreAuthorize("hasRole('ADMIN')")
  public Page<CategoryAdminDto> getAdminCategories(int page, int limit) {
    PageRequest pageRequest = PageRequest.of(page, limit);
    Page<Category> categoryPage = categoryRepository.findAll(pageRequest);
    return categoryPage.map(categoryMapper::categoryToCategoryAdminDto);
  }

  // 관리자용 카테고리 개별 조회 - 카테고리 수정 시 사용
  @PreAuthorize("hasRole('ADMIN')")
  public CategoryAdminDto getAdminCategory(Long categoryId) {
    return categoryRepository.findById(categoryId)
        .map(categoryMapper::categoryToCategoryAdminDto)
        .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));
  }


}
