package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.category.*;
import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.repository.CategoryRepository;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.item.repository.ItemRepository;
import com.spring_boots.spring_boots.orders.repository.OrderItemsRepository;
import com.spring_boots.spring_boots.s3Bucket.service.S3BucketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private CategoryMapper categoryMapper;

  @Mock
  private S3BucketService s3BucketService;

  @Mock
  private ItemRepository itemRepository;
  @Mock
  private OrderItemsRepository orderItemsRepository;

  @InjectMocks
  private CategoryService categoryService;

  private Category mockCategory;
  private CategoryDto mockCategoryDto;
  private CategoryAdminDto mockCategoryAdminDto;
  private MockMultipartFile mockFile;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    mockCategory = Category.builder()
        .id(1L)
        .categoryName("Test Category")
        .categoryThema("Test Thema")
        .displayOrder(1)
        .imageUrl("http://test-url.com/test-image.jpg")
        .build();

    mockCategoryDto = CategoryDto.builder()
        .id(1L)
        .categoryName("Test Category")
        .displayOrder(1)
        .imageUrl("http://test-url.com/test-image.jpg")
        .build();

    mockCategoryAdminDto = CategoryAdminDto.builder()
        .id(1L)
        .categoryName("Test Category")
        .categoryThema("Test Thema")
        .displayOrder(1)
        .build();

    mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
  }

  private final Long INVALID_CATEGORY_ID = 99999L;


  @Test
  @DisplayName("카테고리 저장 확인 테스트")
  void createCategory() throws IOException {
    // given
    CategoryRequestDto requestDto = CategoryRequestDto.builder()
        .categoryName("Test Category")
        .categoryThema("Test Thema")
        .displayOrder(1)
        .build();

    Category mappedCategory = Category.builder()
        .categoryName("Test Category")
        .categoryThema("Test Thema")
        .displayOrder(1)
        .build();

    when(categoryMapper.categoryRequestDtoToCategory(any(CategoryRequestDto.class))).thenReturn(mappedCategory);
    when(s3BucketService.uploadFile(any(MultipartFile.class))).thenReturn("http://test-url.com/image.jpg");
    when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);
    when(categoryMapper.categoryToCategoryResponseDto(any(Category.class))).thenReturn(
        CategoryResponseDto.builder()
            .id(1L)
            .categoryName("Test Category")
            .categoryThema("Test Thema")
            .displayOrder(1)
            .imageUrl("http://test-url.com/image.jpg")
            .build()
    );

    // when
    CategoryResponseDto result = categoryService.createCategory(requestDto, mockFile);

    // then
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Test Category", result.getCategoryName());
    assertEquals("Test Thema", result.getCategoryThema());
    assertEquals(1, result.getDisplayOrder());
    assertEquals("http://test-url.com/image.jpg", result.getImageUrl());

    verify(categoryMapper).categoryRequestDtoToCategory(any(CategoryRequestDto.class));
    verify(s3BucketService).uploadFile(any(MultipartFile.class));
    verify(categoryRepository).save(any(Category.class));
    verify(categoryMapper).categoryToCategoryResponseDto(any(Category.class));
  }


  @Test
  @DisplayName("카테고리 업데이트 확인 테스트")
  void updateCategory() throws IOException {
    // given
    Long categoryId = 1L;
    CategoryRequestDto requestDto = CategoryRequestDto.builder()
        .categoryName("Updated Category")
        .categoryThema("Updated Thema")
        .displayOrder(2)
        .build();

    Category updatedCategory = Category.builder()
        .id(categoryId)
        .categoryName("Updated Category")
        .categoryThema("Updated Thema")
        .displayOrder(2)
        .imageUrl("http://test-url.com/updated-image.jpg")
        .build();

    when(s3BucketService.uploadFile(any(MultipartFile.class))).thenReturn("http://test-url.com/updated-image.jpg");
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));
    when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
    when(categoryMapper.categoryToCategoryResponseDto(any(Category.class))).thenReturn(
        CategoryResponseDto.builder()
            .id(categoryId)
            .categoryName("Updated Category")
            .categoryThema("Updated Thema")
            .displayOrder(2)
            .imageUrl("http://test-url.com/updated-image.jpg")
            .build()
    );

    // when
    CategoryResponseDto result = categoryService.updateCategory(categoryId, requestDto, mockFile);

    // then
    assertNotNull(result);
    assertEquals(categoryId, result.getId());
    assertEquals("Updated Category", result.getCategoryName());
    assertEquals("Updated Thema", result.getCategoryThema());
    assertEquals(2, result.getDisplayOrder());
    assertEquals("http://test-url.com/updated-image.jpg", result.getImageUrl());

    verify(s3BucketService).uploadFile(any(MultipartFile.class));
    verify(categoryRepository).findById(categoryId);
    verify(categoryRepository).save(any(Category.class));
    verify(categoryMapper).categoryToCategoryResponseDto(any(Category.class));

  }

  @Test
  @DisplayName("존재하지 않는 ID로 카테고리 업데이트 시 예외 발생 확인 테스트")
  void updateCategory_유효하지않은ID_예외발생() throws IOException {
    // given
    CategoryRequestDto requestDto = CategoryRequestDto.builder()
        .categoryName("Updated Category")
        .categoryThema("Updated thema")
        .displayOrder(2)
        .build();

    when(categoryRepository.findById(INVALID_CATEGORY_ID)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> categoryService.updateCategory(INVALID_CATEGORY_ID, requestDto, mockFile))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("카테고리를 찾을 수 없습니다: " + INVALID_CATEGORY_ID);

    verify(categoryRepository).findById(INVALID_CATEGORY_ID);
    verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  @DisplayName("카테고리 삭제 확인 테스트")
  void deleteCategory() throws IOException {
    // given
    Long categoryId = 1L;
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));

    // when
    categoryService.deleteCategory(categoryId);

    // then
    verify(categoryRepository).findById(categoryId);
    verify(s3BucketService).deleteFile(anyString());
    verify(categoryRepository).delete(mockCategory);
  }

  @Test
  @DisplayName("존재하지 않는 ID로 카테고리 삭제 시 예외 발생 확인 테스트")
  void deleteCategory_유효하지않은ID_예외발생() {
    // given
    when(categoryRepository.findById(INVALID_CATEGORY_ID)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> categoryService.deleteCategory(INVALID_CATEGORY_ID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("카테고리를 찾을 수 없습니다: " + INVALID_CATEGORY_ID);

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
  void getCategoryDetail_유효하지않은ID_예외발생() {
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
  void getAdminCategory_유효하지않은ID_예외발생() {
    // given
    when(categoryRepository.findById(INVALID_CATEGORY_ID)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> categoryService.getAdminCategory(INVALID_CATEGORY_ID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("카테고리를 찾을 수 없습니다: " + INVALID_CATEGORY_ID);

    verify(categoryRepository).findById(INVALID_CATEGORY_ID);
  }

  @Test
  @DisplayName("특정 displayOrder를 제외한 카테고리 목록 조회 확인 테스트")
  void getCategoriesExcludingDisplayOrder() {
    // given
    int excludeOrder = 0;
    List<Category> categories = Arrays.asList(mockCategory, mockCategory);
    when(categoryRepository.findByDisplayOrderNot(excludeOrder)).thenReturn(categories);

    // when
    List<Category> result = categoryService.getCategoriesExcludingDisplayOrder(excludeOrder);

    // then
    assertEquals(2, result.size());
    verify(categoryRepository).findByDisplayOrderNot(excludeOrder);
  }
}