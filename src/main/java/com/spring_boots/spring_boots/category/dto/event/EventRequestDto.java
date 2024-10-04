package com.spring_boots.spring_boots.category.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EventRequestDto {
  @NotBlank(message = "이벤트 제목은 필수입니다.")
  private String eventTitle;

  @NotBlank(message = "이벤트 내용은 필수입니다.")
  private String eventContent;


  private String thumbnailImageUrl;
  private String contentImageUrl;

  private LocalDate startDate;
  private LocalDate endDate;

//  @NotNull(message = "카테고리 ID는 필수입니다.")
  private Long categoryId;

}
