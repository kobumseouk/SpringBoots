package com.spring_boots.spring_boots.category.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto {
  private Long id;
  private String categoryName;
  private String categoryContent;
  private String imageUrl;
  private int displayOrder;
}

