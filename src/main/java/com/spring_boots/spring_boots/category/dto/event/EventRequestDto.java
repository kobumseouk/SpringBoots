package com.spring_boots.spring_boots.category.dto.event;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class EventRequestDto {
  @NotBlank(message = "이벤트 제목은 필수입니다.")
  private String eventTitle;

  @NotBlank(message = "이벤트 내용은 필수입니다.")
  private String eventContent;

  private LocalDate startDate;
  private LocalDate endDate;

}
