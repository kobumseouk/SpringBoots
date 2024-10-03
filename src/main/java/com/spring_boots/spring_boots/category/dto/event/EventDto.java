package com.spring_boots.spring_boots.category.dto.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
public class EventDto {
  private Long id;

  private String eventTitle;
  private String thumbnailImageUrl;
  private LocalDate startDate;
  private LocalDate endDate;
}
