package com.spring_boots.spring_boots.item.mapper;

import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.dto.UpdateItemDto;
import com.spring_boots.spring_boots.item.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface ItemMapper {
    @Mapping(source = "category.id", target = "categoryId")
    ResponseItemDto toResponseDto(Item item);

}
