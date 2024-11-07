package com.spring_boots.spring_boots.item.mapper;

import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.dto.SearchItemDto;
import com.spring_boots.spring_boots.item.dto.UpdateItemDto;
import com.spring_boots.spring_boots.item.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")

public interface ItemMapper {
    @Mapping(source = "category.id", target = "categoryId")
    ResponseItemDto toResponseDto(Item item);

    @Mappings({
        @Mapping(source = "category.id", target = "categoryId"),
        @Mapping(source = "category.categoryName", target = "categoryName"),
        @Mapping(source = "category.categoryThema", target = "categoryThema")
    })
    SearchItemDto toSearchDto(Item item);

    // 키워드 매칭 필드 정보를 포함한 매핑
    default SearchItemDto toSearchDtoWithMatchedField(Item item, String keyword) {
        SearchItemDto dto = toSearchDto(item);

        // 매칭된 필드 찾기
        if (item.getItemName().toLowerCase().contains(keyword.toLowerCase())) {
            dto.setMatchedField("itemName");
        } else if (item.getCategory().getCategoryName().toLowerCase().contains(keyword.toLowerCase())) {
            dto.setMatchedField("categoryName");
        } else if (item.getCategory().getCategoryThema().toLowerCase().contains(keyword.toLowerCase())) {
            dto.setMatchedField("categoryThema");
        } else if (item.getItemColor().stream()
            .anyMatch(color -> color.toLowerCase().contains(keyword.toLowerCase()))) {
            dto.setMatchedField("color");
        } else if (item.getKeywords().stream()
            .anyMatch(k -> k.toLowerCase().contains(keyword.toLowerCase()))) {
            dto.setMatchedField("keyword");
        }

        return dto;
    }

}
