package com.spring_boots.spring_boots.item.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class ResponseItemDto {

    private Long id;
    private String itemName;
    private Long categoryId;
    private Integer itemPrice;
    private String itemDescription;
    private String itemMaker;
    private String itemColor;
    private String imageUrl;
    private List<String> keywords;
}
