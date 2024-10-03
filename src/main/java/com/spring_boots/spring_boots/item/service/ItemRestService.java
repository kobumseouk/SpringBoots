package com.spring_boots.spring_boots.item.service;

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


    public List<ResponseItemDto> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return items.stream().map(itemMapper::toResponseDto).collect(Collectors.toList());
    }

    public ResponseItemDto getItem(Long id) throws Exception {
        Item item = itemRepository.findById(id).orElseThrow(() -> new Exception("Item not found"));
        return itemMapper.toResponseDto(item);
    }

    public ResponseItemDto createItem(CreateItemDto itemDto) {
        Item created = itemMapper.toEntity(itemDto);
        Item result = itemRepository.save(created);
        return itemMapper.toResponseDto(result);
    }

    public ResponseItemDto updateItem(Long id, UpdateItemDto itemDto) throws Exception {
        Item findItem = itemRepository.findById(id).orElseThrow(() -> new Exception("Item not found"));
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

    public void deleteItem(Long id) throws Exception {
        Item item = itemRepository.findById(id).orElseThrow(() -> new Exception("Item not found"));
        itemRepository.delete(item);
    }
}
