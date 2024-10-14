package com.spring_boots.spring_boots.category.dto.category;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class CategoryRequestDto {

  @NotBlank(message = "카테고리 이름은 필수입니다.")
  private String categoryName;

  @NotBlank(message = "카테고리 테마는 필수입니다.")
  private String categoryThema;

  private String categoryContent;

  @NotNull(message = "카테고리의 배치 순서 설정은 필수입니다.")
  private int displayOrder;

}
