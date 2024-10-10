package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.category.*;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.repository.CategoryRepository;
import com.spring_boots.spring_boots.item.entity.Item;
import com.spring_boots.spring_boots.item.repository.ItemRepository;
import com.spring_boots.spring_boots.orders.repository.OrderItemsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final ItemRepository itemRepository;
  private final OrderItemsRepository orderItemsRepository;
  private final CategoryMapper categoryMapper;


  // 새 카테고리 생성
  @Transactional
  public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
    Category category = categoryMapper.categoryRequestDtoToCategory(requestDto);

    // 같은 테마의 카테고리들 중 displayOrder가 같거나 큰 카테고리들의 순서를 1씩 증가
    categoryRepository.incrementDisplayOrderForSubsequentCategories(
        requestDto.getCategoryThema(),
        requestDto.getDisplayOrder()
    );

    Category updatedCategory = categoryRepository.save(category);

    return categoryMapper.categoryToCategoryResponseDto(updatedCategory);
  }


  // 카테고리 수정
  @Transactional
  public CategoryResponseDto updateCategory(Long categoryId, CategoryRequestDto requestDto) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));

    int oldDisplayOrder = category.getDisplayOrder();
    int newDisplayOrder = requestDto.getDisplayOrder();

    // 배치 순서 조정
    if (oldDisplayOrder != newDisplayOrder) {
      if (oldDisplayOrder < newDisplayOrder) {
        // 카테고리를 뒤로 이동
        categoryRepository.decrementDisplayOrderForIntermediateCategories(
            category.getCategoryThema(),
            oldDisplayOrder + 1,
            newDisplayOrder
        );
      } else {
        // 카테고리를 앞으로 이동
        categoryRepository.incrementDisplayOrderForIntermediateCategories(
            category.getCategoryThema(),
            newDisplayOrder,
            oldDisplayOrder - 1
        );
      }
    }

    // 카테고리 정보 업데이트
    categoryMapper.updateCategoryFromDto(requestDto, category);
    Category updatedCategory = categoryRepository.save(category);

    return categoryMapper.categoryToCategoryResponseDto(updatedCategory);
  }


  // 카테고리 삭제
  @Transactional
  public void deleteCategory(Long categoryId) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));

    /*// 카테고리와 연관된 모든 아이템의 카테고리를 null로 설정 -> item의 category 필드의 nullable = false 문제
    itemRepository.findAllByCategoryId(categoryId)
        .forEach(item -> {
          item.setCategory(null);
          itemRepository.save(item);  // 변경사항을 저장
        });*/
    // 카테고리에 속한 모든 아이템 조회
    List<Item> items = itemRepository.findAllByCategoryId(categoryId);

    for (Item item : items) {
      // 각 아이템과 연관된 주문 아이템 삭제
      orderItemsRepository.deleteAllByItem_ItemId(item.getItemId());

      // 아이템 삭제
      itemRepository.delete(item);
    }

    // 카테고리 삭제
    categoryRepository.delete(category);
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

  // 카테고리 테마에 속한 카테고리 수 측정
  /*public int getCategoryCountByThema(String thema) {
    return categoryRepository.countByCategoryThema(thema);
  }*/


  // 관리자용 카테고리 목록 페이지네이션 적용하여 조회
  public Page<CategoryAdminDto> getAdminCategories(int page, int limit) {
    PageRequest pageRequest = PageRequest.of(page, limit);
    Page<Category> categoryPage = categoryRepository.findAll(pageRequest);
    return categoryPage.map(categoryMapper::categoryToCategoryAdminDto);
  }

  // 관리자용 카테고리 개별 조회 - 카테고리 수정 시 사용
  public CategoryAdminDto getAdminCategory(Long categoryId) {
    return categoryRepository.findById(categoryId)
        .map(categoryMapper::categoryToCategoryAdminDto)
        .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));
  }


}
