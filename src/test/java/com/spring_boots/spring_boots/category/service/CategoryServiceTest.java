package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.category.*;
import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.repository.CategoryRepository;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private CategoryMapper categoryMapper;

  @InjectMocks
  private CategoryService categoryService;

  private Category mockCategory;
  private CategoryDto mockCategoryDto;
  private CategoryAdminDto mockCategoryAdminDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    mockCategory = Category.builder()
        .id(1L)
        .categoryName("Test Category")
        .categoryThema("Test Thema")
        .displayOrder(1)
        .build();

    mockCategoryDto = CategoryDto.builder()
        .id(1L)
        .categoryName("Test Category")
        .displayOrder(1)
        .build();

    mockCategoryAdminDto = CategoryAdminDto.builder()
        .id(1L)
        .categoryName("Test Category")
        .categoryThema("Test Thema")
        .displayOrder(1)
        .build();
  }

  private final Long INVALID_CATEGORY_ID = 99999L;


  @Test
  @DisplayName("카테고리 저장 확인 테스트")
  void createCategory() {
    // given
    CategoryRequestDto requestDto = CategoryRequestDto.builder()
        .categoryName("Test Category")
        .categoryThema("Test Thema")
        .displayOrder(1)
        .build();

    when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);

    // when
    CategoryResponseDto result = categoryService.createCategory(requestDto);

    // then
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Test Category", result.getCategoryName());
    assertEquals("Test Thema", result.getCategoryThema());
    assertEquals(1, result.getDisplayOrder());

    verify(categoryRepository).save(any(Category.class));
  }


  @Test
  @DisplayName("카테고리 업데이트 확인 테스트")
  void updateCategory() {
    // given
    Long categoryId = 1L;
    CategoryRequestDto requestDto = CategoryRequestDto.builder()
        .categoryName("Updated Category")
        .categoryThema("Updated Thema")
        .displayOrder(2)
        .build();

    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));
    when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);

    // when
    CategoryResponseDto result = categoryService.updateCategory(categoryId, requestDto);

    // then
    assertNotNull(result);
    assertEquals(categoryId, result.getId());
    assertEquals("Updated Category", result.getCategoryName());
    assertEquals("Updated Thema", result.getCategoryThema());
    assertEquals(2, result.getDisplayOrder());


    verify(categoryRepository).findById(categoryId);
    verify(categoryRepository).save(any(Category.class));

  }

  @Test
  @DisplayName("존재하지 않는 ID로 카테고리 업데이트 시 예외 발생 확인 테스트")
  void updateCategory_WithInvalidId_ShouldThrowResourceNotFoundException() {
    // given
    CategoryRequestDto requestDto = CategoryRequestDto.builder()
        .categoryName("Updated Category")
        .categoryThema("Updated thema")
        .displayOrder(2)
        .build();

    when(categoryRepository.findById(INVALID_CATEGORY_ID)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> categoryService.updateCategory(INVALID_CATEGORY_ID, requestDto))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("업데이트할 카테고리를 찾을 수 없습니다: " + INVALID_CATEGORY_ID);

    verify(categoryRepository).findById(INVALID_CATEGORY_ID);
    verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  @DisplayName("카테고리 삭제 확인 테스트")
  void deleteCategory() {
    // given
    Long categoryId = 1L;
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));

    // when
    categoryService.deleteCategory(categoryId);

    // then
    verify(categoryRepository).findById(categoryId);
    verify(categoryRepository).delete(mockCategory);
  }

  @Test
  @DisplayName("존재하지 않는 ID로 카테고리 삭제 시 예외 발생 확인 테스트")
  void deleteCategory_WithInvalidId_ShouldThrowResourceNotFoundException() {
    // given
    when(categoryRepository.findById(INVALID_CATEGORY_ID)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> categoryService.deleteCategory(INVALID_CATEGORY_ID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("삭제할 카테고리를 찾을 수 없습니다: " + INVALID_CATEGORY_ID);

    verify(categoryRepository).findById(INVALID_CATEGORY_ID);
    verify(categoryRepository, never()).delete(any(Category.class));
  }

  @Test
  @DisplayName("카테고리 전체 테마 목록 조회 확인 테스트")
  void getAllThemas() {
    // given
    List<String> themas = Arrays.asList("thema1", "thema2", "thema3");
    when(categoryRepository.findDistinctThemas()).thenReturn(themas);

    // when
    List<String> result = categoryService.getAllThemas();

    // then
    assertEquals(themas, result);
    verify(categoryRepository).findDistinctThemas();
  }

  @Test
  @DisplayName("테마별 카테고리 목록 확인 테스트")
  void getCategoriesByThema() {
    // given
    String thema = "TestThema";
    List<Category> categories = Arrays.asList(mockCategory, mockCategory);
    when(categoryRepository.findByCategoryThemaOrderByDisplayOrder(thema)).thenReturn(categories);
    when(categoryMapper.categoryToCategoryDto(any(Category.class))).thenReturn(mockCategoryDto);

    // when
    List<CategoryDto> result = categoryService.getCategoriesByThema(thema);

    // then
    assertEquals(2, result.size());
    verify(categoryRepository).findByCategoryThemaOrderByDisplayOrder(thema);
    verify(categoryMapper, times(2)).categoryToCategoryDto(any(Category.class));
  }

  @Test
  @DisplayName("카테고리 상세 조회 확인 테스트")
  void getCategoryDetail() {
    // given
    Long categoryId = 1L;
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));
    when(categoryMapper.categoryToCategoryDto(mockCategory)).thenReturn(mockCategoryDto);

    // when
    CategoryDto result = categoryService.getCategoryDetail(categoryId);

    // then
    assertEquals(mockCategoryDto, result);
    verify(categoryRepository).findById(categoryId);
    verify(categoryMapper).categoryToCategoryDto(mockCategory);
  }

  @Test
  @DisplayName("존재하지 않는 ID로 카테고리 상세 조회 시 예외 발생 확인 테스트")
  void getCategoryDetail_WithInvalidId_ShouldThrowResourceNotFoundException() {
    // given
    when(categoryRepository.findById(INVALID_CATEGORY_ID)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> categoryService.getCategoryDetail(INVALID_CATEGORY_ID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("조회할 카테고리를 찾을 수 없습니다: " + INVALID_CATEGORY_ID);

    verify(categoryRepository).findById(INVALID_CATEGORY_ID);
  }

  @Test
  @DisplayName("관리자용 카테고리 목록 조회 확인 테스트")
  void getAdminCategories() {
    // given
    int page = 0;
    int limit = 10;
    PageRequest pageRequest = PageRequest.of(page, limit);
    List<Category> categories = Arrays.asList(mockCategory, mockCategory, mockCategory);
    Page<Category> categoryPage = new PageImpl<>(categories, pageRequest, categories.size());

    when(categoryRepository.findAll(pageRequest)).thenReturn(categoryPage);
    when(categoryMapper.categoryToCategoryAdminDto(any(Category.class))).thenReturn(mockCategoryAdminDto);

    // when
    Page<CategoryAdminDto> result = categoryService.getAdminCategories(page, limit);

    // then
    assertEquals(3, result.getContent().size());
    assertEquals(page, result.getNumber());
    assertEquals(limit, result.getSize());
    verify(categoryRepository).findAll(pageRequest);
    verify(categoryMapper, times(3)).categoryToCategoryAdminDto(any(Category.class));
  }

  @Test
  @DisplayName("관리자용 개별 카테고리 조회 확인 테스트")
  void getAdminCategory() {
    // given
    Long categoryId = 1L;
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));
    when(categoryMapper.categoryToCategoryAdminDto(mockCategory)).thenReturn(mockCategoryAdminDto);

    // when
    CategoryAdminDto result = categoryService.getAdminCategory(categoryId);

    // then
    assertEquals(mockCategoryAdminDto, result);
    verify(categoryRepository).findById(categoryId);
    verify(categoryMapper).categoryToCategoryAdminDto(mockCategory);
  }

  @Test
  @DisplayName("존재하지 않는 ID로 관리자용 개별 카테고리 조회 시 예외 발생 확인 테스트")
  void getAdminCategory_WithInvalidId_ShouldThrowResourceNotFoundException() {
    // given
    when(categoryRepository.findById(INVALID_CATEGORY_ID)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> categoryService.getAdminCategory(INVALID_CATEGORY_ID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("카테고리를 찾을 수 없습니다: " + INVALID_CATEGORY_ID);

    verify(categoryRepository).findById(INVALID_CATEGORY_ID);
  }


}