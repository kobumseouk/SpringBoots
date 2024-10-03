package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.category.dto.*;
import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
  @Transactional
  public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
    Category category = categoryMapper.categoryRequestDtoToCategory(requestDto);

    return categoryMapper.categoryToCategoryResponseDto(categoryRepository.save(category));
  }


  // 카테고리 수정
  @Transactional
  public CategoryResponseDto updateCategory(Long categoryId, CategoryRequestDto requestDto) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));
    categoryMapper.updateCategoryFromDto(requestDto, category);

    return categoryMapper.categoryToCategoryResponseDto(categoryRepository.save(category));
  }


  // 카테고리 삭제
  @Transactional
  public void deleteCategory(Long categoryId) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("삭제할 카테고리를 찾을 수 없습니다: " + categoryId));
    categoryRepository.delete(category);
  }


  // 카테고리 전체 테마 목록 조회
  public List<String> getAllThemes() {
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
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));
    return categoryMapper.categoryToCategoryDto(category);
  }


  // 관리자용 카테고리 목록 페이지네이션 적용하여 조회
  public List<CategoryAdminDto> getAdminCategories(int page, int limit) {
    PageRequest pageRequest = PageRequest.of(page, limit);
    Page<Category> categoryPage = categoryRepository.findAll(pageRequest);
    return categoryPage.getContent().stream()
        .map(categoryMapper::categoryToCategoryAdminDto)
        .collect(Collectors.toList());
  }


}
