package com.spring_boots.spring_boots.item.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.repository.CategoryRepository;
import com.spring_boots.spring_boots.common.config.error.BadRequestException;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.dto.SearchItemDto;
import com.spring_boots.spring_boots.item.dto.UpdateItemDto;
import com.spring_boots.spring_boots.item.entity.Item;
import com.spring_boots.spring_boots.item.mapper.ItemMapper;
import com.spring_boots.spring_boots.item.repository.ItemRepository;
import com.spring_boots.spring_boots.s3Bucket.service.S3BucketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final S3BucketService s3BucketService;
    private final SearchHistoryService searchHistoryService;
    private final CategoryRepository categoryRepository;
    private final AmazonS3 amazonS3;


    @Value("${aws.s3.bucket.name}")
    private String bucketName;


    // Item 전체 보기
    public Page<ResponseItemDto> getAllItems(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Item> itemsPage = itemRepository.findAll(pageable);
        return itemsPage.map(itemMapper::toResponseDto);
    }

    // Item 단일 보기
    public ResponseItemDto getItem(Long id) {
        return itemMapper.toResponseDto(findItemById(id));
    }

    // item_id에 해당하는 상품찾기
    private Item findItemById(Long id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: " + id));
    }

    // Item 만들기
    @Transactional
    public ResponseItemDto createItem(CreateItemDto itemDto, MultipartFile file) throws IOException {
        Category category = categoryRepository.findById(itemDto.getCategoryId()) // categoryId로 Category 객체 조회
                .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다.: " + itemDto.getCategoryId()));
        if (file != null && !file.isEmpty()) {
            validateImageFile(file);   // 이미지 크기 검증
            String imageUrl = s3BucketService.uploadFile(file);  // 이미지 파일 업로드
            itemDto.setImageUrl(imageUrl);  // DTO에 이미지 URL 설정
        }

        Item created = itemDto.toEntity();
        created.setCategory(category);

        Item result = itemRepository.save(created);
        return itemMapper.toResponseDto(result);
    }

    // 이미지 크기 유효성 검증
    private void validateImageFile(MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("이미지_크기_초과", "이미지 파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }

    // Item 수정하기
    @Transactional
    public ResponseItemDto updateItem(Long id, UpdateItemDto itemDto) throws IOException {
        Item findItem = itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("아이템을 찾을 수 없습니다: " + id));
        String existingImageUrl = findItem.getImageUrl(); // 기존 저장된 이미지 URL 담기


        //Item Name 수정
        Optional.ofNullable(itemDto.getItemName())
                .ifPresent(findItem::setItemName);

        //Item Price 수정
        Optional.ofNullable(itemDto.getItemPrice())
                .ifPresent(findItem::setItemPrice);

        //Item Description 수정
        Optional.ofNullable(itemDto.getItemDescription())
                .ifPresent(findItem::setItemDescription);

        //Item Maker 수정
        Optional.ofNullable(itemDto.getItemMaker())
                .ifPresent(findItem::setItemMaker);

        //Item Color 수정
        if (itemDto.getItemColor() != null) {
            findItem.getItemColor().clear();
            findItem.getItemColor().addAll(itemDto.getItemColor());
        }

        updateItemImage(findItem, itemDto.getFile(), existingImageUrl);
        updateItemCategory(findItem, itemDto.getCategoryId());

        //키워드 수정
        if (itemDto.getKeywords() != null) {
            findItem.getKeywords().clear(); // 기존 키워드 삭제
            findItem.getKeywords().addAll(itemDto.getKeywords()); // 새로운 키워드 추가
        }

        Item updated = itemRepository.save(findItem);
        return itemMapper.toResponseDto(updated);
    }

    // S3 상품 이미지 업데이트
    private void updateItemImage(Item item, MultipartFile file, String existingImageUrl) throws IOException {
        if (file != null && !file.isEmpty()) {
            validateImageFile(file);
            deleteItemImage(existingImageUrl);
            String newImageUrl = s3BucketService.uploadFile(file);
            item.setImageUrl(newImageUrl);
        }
    }

    // 상품의 카테고리 변경
    private void updateItemCategory(Item item, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));
            item.setCategory(category);
        }
    }

    // Item 삭제하기
    @Transactional
    public void deleteItem(Long id) {
        Item item = findItemById(id);
        deleteItemImage(item.getImageUrl());
        itemRepository.delete(item);
    }

    // Item S3 이미지 삭제하기
    private void deleteItemImage(String imageUrl) {
        if (imageUrl != null) {
            String key = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        }
    }

    // Category로 Item 조회 리스트
    public Page<ResponseItemDto> getItemsByCategoryWithSorting(Long categoryId, String sort, int page, int limit) {
        Pageable pageable = createPageableWithSort(sort, page, limit);
        Page<Item> itemsPage = itemRepository.findAllByCategoryId(categoryId, pageable);
        return itemsPage.map(itemMapper::toResponseDto);
    }

    // 검색한 아이템 키워드 정렬 옵션
    public Page<SearchItemDto> searchAndSortItems(String keyword, String sort, int page, int limit, Long userId) {
        // 검색어 저장
        if (userId != null) {
            searchHistoryService.saveSearchKeyword(userId, keyword);
        }

        // 검색어를 소문자로 변환
        String searchKeyword = keyword.toLowerCase();
        Pageable pageable = createPageableWithSort(sort, page, limit);

        // 먼저 일반 필드 검색 시도 (상품명, 제조사, 카테고리명, 색상)
        Page<Item> items = itemRepository.findByNonKeywordSearch(searchKeyword, pageable);
        Page<SearchItemDto> searchResult = items.map(item ->
            itemMapper.toSearchDtoWithMatchedField(item, searchKeyword));

        // 일반 검색 결과가 없으면 키워드 매칭 검색 시도
        if (searchResult.isEmpty()) {
            Page<Item> keywordItems = itemRepository.findByKeywordSearch(searchKeyword, pageable);
            List<SearchItemDto> matchedItems = keywordItems.getContent().stream()
                .filter(item -> item.getKeywords().stream()
                    .anyMatch(k -> k.toLowerCase().equals(searchKeyword) ||
                        Arrays.asList(k.toLowerCase().split(" "))
                            .stream()
                            .anyMatch(word -> word.equals(searchKeyword))))
                .map(item -> itemMapper.toSearchDtoWithMatchedField(item, searchKeyword))
                .collect(Collectors.toList());

            return new PageImpl<>(matchedItems, pageable, matchedItems.size());
        }

        return searchResult;

        // return items.map(item -> itemMapper.toSearchDtoWithMatchedField(item, searchKeyword));
    }

    // 테마별 정렬된 모든 아이템 조회
    public Page<ResponseItemDto> getItemsByCategoryThemaWithSorting(String thema, String sort, int page, int limit) {
        Pageable pageable = createPageableWithSort(sort, page, limit);
        Page<Item> itemsPage = itemRepository.findByCategory_CategoryThema(thema, pageable);
        return itemsPage.map(itemMapper::toResponseDto);
    }

    // 정렬
    private Pageable createPageableWithSort(String sort, int page, int limit) {
        Sort sortOrder;
        switch (sort) {
            case "best":    // 판매량순
                sortOrder = Sort.by(Sort.Direction.DESC, "itemQuantity");
                break;
            case "price-asc":  // 낮은 가격순
                sortOrder = Sort.by(Sort.Direction.ASC, "itemPrice");
                break;
            case "price-desc":  // 높은 가격순
                sortOrder = Sort.by(Sort.Direction.DESC, "itemPrice");
                break;
            case "newest":   // 최신순
                sortOrder = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
            default:   // 기본 상품 id 오름차순
                sortOrder = Sort.by(Sort.Direction.ASC, "id");
                break;
        }
        return PageRequest.of(page, limit, sortOrder);
    }

    // 상품 이름을 이용하여 검색
    public Page<ResponseItemDto> searchItemsByName(String itemName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Item> itemsPage = itemRepository.findByItemNameContainingIgnoreCase(itemName, pageable);
        return itemsPage.map(itemMapper::toResponseDto);
    }

}


