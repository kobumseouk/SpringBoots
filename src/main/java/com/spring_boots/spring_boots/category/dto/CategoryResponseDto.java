package com.spring_boots.spring_boots.category.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class CategoryResponseDto {
  private Long id;
  private String categoryName;
  private String categoryThema;
  private String categoryContent;
  private String imageUrl;
  private int displayOrder;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
