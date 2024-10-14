package com.spring_boots.spring_boots.item.service;

import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.dto.UpdateItemDto;
import com.spring_boots.spring_boots.item.entity.Item;
import com.spring_boots.spring_boots.item.mapper.ItemMapper;
import com.spring_boots.spring_boots.item.repository.ItemRepository;
import com.spring_boots.spring_boots.s3Bucket.service.S3BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemRestService {
    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final S3BucketService s3BucketService;

    @Autowired
    public ItemRestService(ItemMapper itemMapper, ItemRepository itemRepository, S3BucketService s3BucketService) {
        this.itemMapper = itemMapper;
        this.itemRepository = itemRepository;
        this.s3BucketService = s3BucketService;
    }

    // Item 전체 보기
    public List<ResponseItemDto> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return items.stream().map(itemMapper::toResponseDto).collect(Collectors.toList());
    }

    // Item 단일 보기
    public ResponseItemDto getItem(Long itemId)  {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: " + itemId));
        return itemMapper.toResponseDto(item);
    }

    // Item 만들기
    public ResponseItemDto createItem(CreateItemDto itemDto, MultipartFile file) {
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
        Item result = itemRepository.save(created);
        return itemMapper.toResponseDto(result);
    }

    // Item 수정하기
    public ResponseItemDto updateItem(Long itemId, UpdateItemDto itemDto)  {
        Item findItem = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("아이템을 찾을 수 없습니다: " + itemId));

        //Item Name 수정
        Optional.ofNullable(itemDto.getItemName())
                .ifPresent(findItem::setItemName);

        //Item Category 수정
        Optional.ofNullable(itemDto.getCategory())
                .ifPresent(findItem::setCategory);

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
        Optional.ofNullable(itemDto.getImageUrl())
                .ifPresent(findItem::setImageUrl);

        Item updated = itemRepository.save(findItem);
        return itemMapper.toResponseDto(updated);
    }

    // Item 삭제하기
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: "+ itemId));
        itemRepository.delete(item);
    }
}
