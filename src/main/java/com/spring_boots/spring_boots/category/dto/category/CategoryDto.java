package com.spring_boots.spring_boots.category.dto.category;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryDto {
  private Long id;
  private String categoryName;
  private String categoryContent;
  private String imageUrl;
  private int displayOrder;
}

