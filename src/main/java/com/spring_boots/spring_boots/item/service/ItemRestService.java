package com.spring_boots.spring_boots.item.service;

import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.dto.UpdateItemDto;
import com.spring_boots.spring_boots.item.entity.Item;
import com.spring_boots.spring_boots.item.mapper.ItemMapper;
import com.spring_boots.spring_boots.item.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemRestService {
    private final ItemMapper itemMapper;

    private final ItemRepository itemRepository;

    @Autowired
    public ItemRestService(ItemMapper itemMapper, ItemRepository itemRepository) {
        this.itemMapper = itemMapper;
        this.itemRepository = itemRepository;
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
    public ResponseItemDto createItem(CreateItemDto itemDto) {
        Item created = itemMapper.toEntity(itemDto);
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
