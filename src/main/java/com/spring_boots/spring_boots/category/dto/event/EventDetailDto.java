package com.spring_boots.spring_boots.category.dto.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@Builder
public class EventDetailDto {

  private Long id;

  private String eventTitle;
  private String eventContent;
  private String thumbnailImageUrl;
  private String contentImageUrl;
  private LocalDate startDate;
  private LocalDate endDate;
  private Boolean isActive;

  // TODO: 독립적 사용의 경우 제거
  private String categoryName;

}
