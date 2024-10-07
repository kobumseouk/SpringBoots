package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.category.*;
import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.repository.CategoryRepository;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private CategoryMapper categoryMapper;

  @InjectMocks
  private CategoryService categoryService;

  private final Long INVALID_CATEGORY_ID = 99999L;


  @Test
  @DisplayName("카테고리 저장 확인 테스트")
  void createCategoryTest() {
    // given
    CategoryRequestDto requestDto = CategoryRequestDto.builder()
        .categoryName("Test Category")
        .categoryThema("Test thema")
        .displayOrder(1)
        .build();

    Category category = new Category();

    CategoryResponseDto responseDto = CategoryResponseDto.builder()
        .id(1L)
        .categoryName("Test Category")
        .categoryThema("Test thema")
        .displayOrder(1)
        .build();

    // 각 모킹된 메서드에 대해 호출 시 반환될 값을 설정하여 특정 입력에 대해 기대하는 출력을 정의
    when(categoryMapper.categoryRequestDtoToCategory(any(CategoryRequestDto.class))).thenReturn(category);
    when(categoryRepository.save(any(Category.class))).thenReturn(category);
    when(categoryMapper.categoryToCategoryResponseDto(any(Category.class))).thenReturn(responseDto);

    // when
    CategoryResponseDto result = categoryService.createCategory(requestDto);

    // then - 중요한 테스트 위주로 먼저 실행!!


    // isEqualTo()만 사용하는 것과는 달리 두 객체의 모든 속성이 정확히 일치하는지 재귀적 비교 + isNotNull() 기능 대체
    assertThat(result).usingRecursiveComparison().isEqualTo(responseDto);

    verify(categoryMapper).categoryRequestDtoToCategory(eq(requestDto));
    verify(categoryRepository).save(any(Category.class));
    verify(categoryMapper).categoryToCategoryResponseDto(eq(category));

  }


  @Test
  @DisplayName("카테고리 업데이트 확인 테스트")
  void updateCategory() {
    // given
    Long categoryId = 1L;
    CategoryRequestDto requestDto = CategoryRequestDto.builder()
        .categoryName("Updated Category")
        .categoryThema("Updated thema")
        .displayOrder(2)
        .build();

    Category existingCategory = Category.builder()
        .id(categoryId)
        .categoryName("Original Category")
        .categoryThema("Original thema")
        .displayOrder(1)
        .build();

    Category updatedCategory = Category.builder()
        .id(categoryId)
        .categoryName("Updated Category")
        .categoryThema("Updated thema")
        .displayOrder(2)
        .build();

    CategoryResponseDto responseDto = CategoryResponseDto.builder()
        .id(categoryId)
        .categoryName("Updated Category")
        .categoryThema("Updated thema")
        .displayOrder(2)
        .build();

    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
    when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
    when(categoryMapper.categoryToCategoryResponseDto(any(Category.class))).thenReturn(responseDto);
    // updateCategoryFromDto() 메서드의 동작을 검증만 하고, 실제로 엔티티를 수정 x
    doNothing().when(categoryMapper).updateCategoryFromDto(any(CategoryRequestDto.class), any(Category.class));

    // when
    CategoryResponseDto result = categoryService.updateCategory(categoryId, requestDto);

    // then
    assertThat(result).usingRecursiveComparison().isEqualTo(responseDto);

    verify(categoryRepository).findById(categoryId);
    verify(categoryRepository).save(any(Category.class));
    verify(categoryMapper).updateCategoryFromDto(eq(requestDto), eq(existingCategory));
    verify(categoryMapper).categoryToCategoryResponseDto(any(Category.class));

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
    verify(categoryRepository, times(0)).save(any(Category.class));

  }


  @Test
  @DisplayName("카테고리 삭제 확인 테스트")
  void deleteCategory() {
    // given
    Long categoryId = 1L;
    Category category = new Category();
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

    // when
    assertThatCode(() -> categoryService.deleteCategory(categoryId)).doesNotThrowAnyException();

    // then
    verify(categoryRepository).findById(categoryId);
    verify(categoryRepository).delete(category);
  }

  @Test
  @DisplayName("존재하지 않는 ID로 카테고리 삭제 시 예외 발생 확인 테스트")
  void deleteCategory_WithInvalidId_ShouldThrowResourceNotFoundException() {
    // given
    // Long categoryId = 99999L;
    Category category = new Category();
    when(categoryRepository.findById(INVALID_CATEGORY_ID)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> categoryService.deleteCategory(INVALID_CATEGORY_ID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("삭제할 카테고리를 찾을 수 없습니다: " + INVALID_CATEGORY_ID);
    
    verify(categoryRepository).findById(INVALID_CATEGORY_ID);
    verify(categoryRepository, times(0)).delete(category);
  }


  @Test
  @DisplayName("카테고리 전체 테마 목록 조회 확인 테스트")
  void getAllthemas() {
    // given
    List<String> themas = Arrays.asList("thema1", "thema2", "thema3");
    when(categoryRepository.findDistinctThemas()).thenReturn(themas);

    // when
    List<String> result = categoryService.getAllThemas();

    // then
    assertThat(result).usingRecursiveComparison().isEqualTo(themas);
    verify(categoryRepository).findDistinctThemas();
  }


  @Test
  @DisplayName("테마별 카테고리 목록 확인 테스트")
  void getCategoriesByThema() {
    // given
    String thema = "TestThema";

    List<Category> categories = Arrays.asList(
        Category.builder().id(1L).categoryThema(thema).categoryName("Category1").displayOrder(1).build(),
        Category.builder().id(2L).categoryThema(thema).categoryName("Category2").displayOrder(2).build()
    );

    List<CategoryDto> categoryDtos = Arrays.asList(
        CategoryDto.builder().id(1L).categoryName("Category1").displayOrder(1).build(),
        CategoryDto.builder().id(2L).categoryName("Category2").displayOrder(2).build()
    );

    when(categoryRepository.findByCategoryThemaOrderByDisplayOrder(thema)).thenReturn(categories);
    when(categoryMapper.categoryToCategoryDto(any(Category.class))).thenAnswer(invocation -> {
      Category category = invocation.getArgument(0);
      return CategoryDto.builder()
          .id(category.getId())
          .categoryName(category.getCategoryName())
          .displayOrder(category.getDisplayOrder())
          .build();
    });

    // when
    List<CategoryDto> result = categoryService.getCategoriesByThema(thema);

    // then
    assertThat(result).usingRecursiveComparison().isEqualTo(categoryDtos);

    verify(categoryRepository).findByCategoryThemaOrderByDisplayOrder(thema);
    verify(categoryMapper, times(2)).categoryToCategoryDto(any(Category.class));

  }


  @Test
  @DisplayName("카테고리 상세 조회 확인 테스트")
  void getCategoryDetail() {
    // given
    Long categoryId = 1L;
    Category category = Category.builder()
        .id(categoryId)
        .categoryName("Test Category")
        .categoryThema("Test Thema")
        .displayOrder(1)
        .build();

    CategoryDto categoryDto = CategoryDto.builder()
        .id(categoryId)
        .categoryName("Test Category")
        .displayOrder(1)
        .build();

    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
    when(categoryMapper.categoryToCategoryDto(category)).thenReturn(categoryDto);

    // when
    CategoryDto result = categoryService.getCategoryDetail(categoryId);

    // then
    assertThat(result).usingRecursiveComparison().isEqualTo(categoryDto);

    verify(categoryRepository).findById(categoryId);
    verify(categoryMapper).categoryToCategoryDto(category);


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
    List<Category> categories = Arrays.asList(
        Category.builder().id(1L).categoryThema("Thema1").categoryName("Category1").displayOrder(1).build(),
        Category.builder().id(2L).categoryThema("Thema1").categoryName("Category2").displayOrder(2).build(),
        Category.builder().id(3L).categoryThema("Thema2").categoryName("Category1").displayOrder(1).build()
    );
    Page<Category> categoryPage = new PageImpl<>(categories, pageRequest, categories.size());

    List<CategoryAdminDto> categoryAdminDtos = Arrays.asList(
        CategoryAdminDto.builder().id(1L).categoryThema("Thema1").categoryName("Category1").displayOrder(1).build(),
        CategoryAdminDto.builder().id(2L).categoryThema("Thema1").categoryName("Category2").displayOrder(2).build(),
        CategoryAdminDto.builder().id(3L).categoryThema("Thema2").categoryName("Category1").displayOrder(1).build()
    );
    Page<CategoryAdminDto> dtoPage = new PageImpl<>(categoryAdminDtos, pageRequest, categoryAdminDtos.size());

    when(categoryRepository.findAll(pageRequest)).thenReturn(categoryPage);
    when(categoryMapper.categoryToCategoryAdminDto(any(Category.class))).thenAnswer(invocation -> {
      Category category = invocation.getArgument(0);
      return CategoryAdminDto.builder()
          .id(category.getId())
          .categoryName(category.getCategoryName())
          .categoryThema(category.getCategoryThema())
          .displayOrder(category.getDisplayOrder())
          .build();
    });

    // when
    Page<CategoryAdminDto> result = categoryService.getAdminCategories(page, limit);

    // then
    assertThat(result).usingRecursiveComparison().isEqualTo(dtoPage);
    assertThat(result.getNumber()).isEqualTo(page);
    assertThat(result.getSize()).isEqualTo(limit);

    verify(categoryRepository).findAll(pageRequest);
    verify(categoryMapper, times(3)).categoryToCategoryAdminDto(any(Category.class));

  }


}