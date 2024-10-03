package com.spring_boots.spring_boots.category.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class CategoryAdminDto {
  private Long id;
  private String categoryName;
  private String categoryThema;
  private int displayOrder;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


