package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.category.*;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.repository.CategoryRepository;
import com.spring_boots.spring_boots.item.entity.Item;
import com.spring_boots.spring_boots.item.repository.ItemRepository;
import com.spring_boots.spring_boots.orders.repository.OrderItemsRepository;
import com.spring_boots.spring_boots.s3Bucket.service.S3BucketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final ItemRepository itemRepository;
  private final OrderItemsRepository orderItemsRepository;
  private final S3BucketService s3BucketService;
  private final CategoryMapper categoryMapper;


  // 새 카테고리 생성
  @Transactional
  public CategoryResponseDto createCategory(CategoryRequestDto requestDto, MultipartFile file) throws IOException {
    String imageUrl = null;
    if (file != null && !file.isEmpty()) {
      imageUrl = s3BucketService.uploadFile(file);
    }

    Category category = categoryMapper.categoryRequestDtoToCategory(requestDto);
    category.setImageUrl(imageUrl);

    // 같은 테마의 카테고리들 중 displayOrder가 같거나 큰 카테고리들의 순서를 1씩 증가
    categoryRepository.incrementDisplayOrderForSubsequentCategories(
        requestDto.getCategoryThema(),
        requestDto.getDisplayOrder()
    );

    Category savedCategory = categoryRepository.save(category);

    return categoryMapper.categoryToCategoryResponseDto(savedCategory);
  }


  // 카테고리 수정
  @Transactional
  public CategoryResponseDto updateCategory(Long categoryId, CategoryRequestDto requestDto, MultipartFile file) throws IOException {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));

    if (file != null && !file.isEmpty()) {
      String newImageUrl = s3BucketService.uploadFile(file);
      // 기존 이미지가 있다면 S3에서 삭제
      if (category.getImageUrl() != null) {
        s3BucketService.deleteFile(category.getImageUrl().substring(category.getImageUrl().lastIndexOf("/") + 1));
      }
      category.setImageUrl(newImageUrl);
    }

    int oldDisplayOrder = category.getDisplayOrder();
    int newDisplayOrder = requestDto.getDisplayOrder();

    // 배치 순서 조정
    if (oldDisplayOrder != newDisplayOrder) {
      if (oldDisplayOrder < newDisplayOrder) {
        // 카테고리를 뒤로 이동
        categoryRepository.decrementDisplayOrderForIntermediateCategories(
            category.getCategoryThema(),
            oldDisplayOrder,    // 기존 위치부터
            newDisplayOrder          //  수정 할 위치까지
        );
      } else {
        // 카테고리를 앞으로 이동
        categoryRepository.incrementDisplayOrderForIntermediateCategories(
            category.getCategoryThema(),
            newDisplayOrder,   // 수정 할 위치부터
            oldDisplayOrder     // 기존 위치까지
        );
      }
    }

    // 카테고리 정보 업데이트
    categoryMapper.updateCategoryFromDto(requestDto, category);
    category.setDisplayOrder(newDisplayOrder);  // 새로운 배치 순서 설정

    // 카테고리 정보 업데이트
    Category updatedCategory = categoryRepository.save(category);

    return categoryMapper.categoryToCategoryResponseDto(updatedCategory);
  }


  // 카테고리 삭제
  @Transactional
  public void deleteCategory(Long categoryId) throws IOException {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));

    // 삭제할 카테고리의 theme와 displayOrder 저장
    String categoryThema = category.getCategoryThema();
    int deleteDisplayOrder = category.getDisplayOrder();
    int totalCategories = categoryRepository.countByCategoryThema(categoryThema);  // 테마가 동일한 카테고리의 총 개수

    // 카테고리 이미지가 있다면 S3에서 삭제
    if (category.getImageUrl() != null) {
      String imageKey = extractKeyFromUrl(category.getImageUrl());
      s3BucketService.deleteFile(imageKey);
    }

    // 카테고리에 속한 모든 아이템 조회
    List<Item> items = itemRepository.findAllByCategoryId(categoryId);
    for (Item item : items) {
      // 아이템 이미지가 있다면 S3에서 삭제
      if (item.getImageUrl() != null) {
        String imageKey = extractKeyFromUrl(item.getImageUrl());
        s3BucketService.deleteFile(imageKey);
      }
      // 각 아이템과 연관된 주문 아이템 삭제
      orderItemsRepository.deleteAllByItem_Id(item.getId());
      // 아이템 삭제
      itemRepository.delete(item);
    }

    // 삭제된 카테고리보다 큰 displayOrder를 가진 카테고리들의 순서를 1씩 감소
    categoryRepository.decrementDisplayOrderForIntermediateCategories(
        categoryThema,
        deleteDisplayOrder,  // 삭제된 카테고리의 순서
        totalCategories            // 해당 테마의 최대 순서값
    );


    // 카테고리 삭제
    categoryRepository.delete(category);


  }

  // URL에서 S3 키를 추출
  private String extractKeyFromUrl(String url) {
    return url.substring(url.lastIndexOf("/") + 1);
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
        .orElseThrow(() -> new ResourceNotFoundException("조회할 카테고리를 찾을 수 없습니다: " + categoryId));
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


  // displayOrder가 1이 아닌 카테고리를 반환
  public List<Category> getCategoriesExcludingDisplayOrder(int displayOrder) {
    return categoryRepository.findByDisplayOrderNot(displayOrder);
  }
}
