package com.spring_boots.spring_boots.item.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.repository.CategoryRepository;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.dto.UpdateItemDto;
import com.spring_boots.spring_boots.item.entity.Item;
import com.spring_boots.spring_boots.item.mapper.ItemMapper;
import com.spring_boots.spring_boots.item.repository.ItemRepository;
import com.spring_boots.spring_boots.s3Bucket.service.S3BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemRestService {
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final S3BucketService s3BucketService;
    private final CategoryRepository categoryRepository;
    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Autowired
    public ItemRestService(ItemMapper itemMapper,
                           ItemRepository itemRepository,
                           S3BucketService s3BucketService,
                           CategoryRepository categoryRepository,
                           AmazonS3 amazonS3) {
        this.itemMapper = itemMapper;
        this.itemRepository = itemRepository;
        this.s3BucketService = s3BucketService;
        this.categoryRepository = categoryRepository;
        this.amazonS3 = amazonS3;
    }


    // Item 전체 보기
    public List<ResponseItemDto> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return items.stream().map(itemMapper::toResponseDto).collect(Collectors.toList());
    }

    // Item 단일 보기
    public ResponseItemDto getItem(Long itemId)  {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: " + itemId));
        System.out.println("Retrieved Item Image URL: " + item.getImageUrl());
        return itemMapper.toResponseDto(item);
    }

    // Item 만들기
    public ResponseItemDto createItem(CreateItemDto itemDto, MultipartFile file) {
        Category category = categoryRepository.findById(itemDto.getCategoryId()) // categoryId로 Category 객체 조회
                .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다.: " + itemDto.getCategoryId()));
        String imageUrl = null;

        if (file != null && !file.isEmpty()) { // 이미지 파일 존재 유무 확인
            try {
                imageUrl = s3BucketService.uploadFile(file); // s3에 파일 업로드
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 실패: " + e.getMessage());
            }
        }
        itemDto.setImageUrl(imageUrl); // DTO에 이미지 URL 설정

        Item created = itemDto.toEntity();
        created.setCategory(category);

        Item result = itemRepository.save(created);
        return itemMapper.toResponseDto(result);
    }

    // Item 수정하기
    public ResponseItemDto updateItem(Long itemId, UpdateItemDto itemDto) throws IOException {
        Item findItem = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("아이템을 찾을 수 없습니다: " + itemId));

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
        Optional.ofNullable(itemDto.getItemColor())
                .ifPresent(findItem::setItemColor);

        //Item Size 수정
        Optional.ofNullable(itemDto.getItemSize())
                .ifPresent(findItem::setItemSize);

        //Item Image 수정
        if (itemDto.getFile() != null && !itemDto.getFile().isEmpty()) { // 수정하기 위해 HTML에 등록한 이미지 파일이 null값이 아닌 경우 동작
            if (existingImageUrl != null) { // 기존 저장된 URL이 null인지 아닌지 체크
                String key = existingImageUrl.substring(existingImageUrl.lastIndexOf("/") + 1);
                amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
            }

            String newImageUrl = s3BucketService.uploadFile(itemDto.getFile());
            findItem.setImageUrl(newImageUrl);
        } else {
            findItem.setImageUrl(existingImageUrl);
        }

        Item updated = itemRepository.save(findItem);
        return itemMapper.toResponseDto(updated);
    }

    // Item 삭제하기
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: "+ itemId));

        String imageUrl = item.getImageUrl();
        if (imageUrl != null) {

            String key = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        }

        itemRepository.delete(item);
    }

    // Category로 Item 조회 리스트
    public List<ResponseItemDto> getItemsByCategory(Long categoryId) {
        List<Item> items = itemRepository.findAllByCategoryId(categoryId);
        return items.stream().map(itemMapper::toResponseDto).collect(Collectors.toList());

    }

    // 검색한 아이템 키워드 정렬 옵션
    public Page<ResponseItemDto> searchAndSortItems(String keyword, String sort, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Item> itemsPage;

        switch (sort) {
            case "price-asc":
                // 낮은 가격순으로 정렬
                pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "itemPrice"));
                break;
            case "price-desc":
                // 높은 가격순으로 정렬
                pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "itemPrice"));
                break;
            case "newest":
                // 최신순으로 정렬
                pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
                break;
            default:
                // 기본 상품 id별 정렬 유지
                break;
        }

        itemsPage = itemRepository.findByKeywordsContainingIgnoreCase(keyword, pageable);
        return itemsPage.map(itemMapper::toResponseDto);
    }
}


