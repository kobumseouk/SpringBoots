package com.spring_boots.spring_boots.item.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class SearchItemDto extends ResponseItemDto {
  private String categoryName;
  private String categoryThema;

  // 어떤 필드에서 매칭되었는지 표시 ("itemName", "categoryName", "categoryThema", "color", "keyword")
  private String matchedField;
}
