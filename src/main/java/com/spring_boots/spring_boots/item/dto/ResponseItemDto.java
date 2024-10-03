package com.spring_boots.spring_boots.item.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ResponseItemDto {
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private String maker;
    private String color;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer size;
}
