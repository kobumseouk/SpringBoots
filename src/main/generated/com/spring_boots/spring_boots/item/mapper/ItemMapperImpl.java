package com.spring_boots.spring_boots.item.mapper;

import com.spring_boots.spring_boots.item.dto.CreateItemDto;
import com.spring_boots.spring_boots.item.dto.ResponseItemDto;
import com.spring_boots.spring_boots.item.dto.UpdateItemDto;
import com.spring_boots.spring_boots.item.entity.Item;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-04T14:56:55+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.9 (GraalVM Community)"
)
@Component
public class ItemMapperImpl implements ItemMapper {

    @Override
    public ResponseItemDto toResponseDto(Item item) {
        if ( item == null ) {
            return null;
        }

        ResponseItemDto responseItemDto = new ResponseItemDto();

        responseItemDto.setCreatedAt( item.getCreatedAt() );
        responseItemDto.setUpdatedAt( item.getUpdatedAt() );

        return responseItemDto;
    }

    @Override
    public Item toEntity(CreateItemDto dto) {
        if ( dto == null ) {
            return null;
        }

        Item item = new Item();

        return item;
    }

    @Override
    public Item toEntity(UpdateItemDto dto) {
        if ( dto == null ) {
            return null;
        }

        Item item = new Item();

        item.setCategory( dto.getCategory() );
        item.setItemName( dto.getItemName() );
        item.setItemPrice( dto.getItemPrice() );
        item.setItemDescription( dto.getItemDescription() );
        item.setItemMaker( dto.getItemMaker() );
        item.setItemColor( dto.getItemColor() );
        item.setImageUrl( dto.getImageUrl() );
        item.setItemSize( dto.getItemSize() );

        return item;
    }
}
