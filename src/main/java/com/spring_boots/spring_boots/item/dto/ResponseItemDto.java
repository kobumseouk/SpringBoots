package com.spring_boots.spring_boots.item.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@SuperBuilder
public class ResponseItemDto {

    private Long id;
    private String itemName;
    private Long categoryId;
    private Long itemPrice;
    private String itemDescription;
    private String itemMaker;
    private List<String> itemColor;
    private String imageUrl;
    private List<String> keywords;
    private int itemQuantity;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
