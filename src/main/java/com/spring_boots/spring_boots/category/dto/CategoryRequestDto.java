package com.spring_boots.spring_boots.category.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CategoryRequestDto {

  @NotBlank(message = "카테고리 이름은 필수입니다.")
  private String categoryName;

  @NotBlank(message = "카테고리 테마는 필수입니다.")
  private String categoryThema;

  private String categoryContent;

  private String imageUrl;

  @NotNull
  private int displayOrder;

}
