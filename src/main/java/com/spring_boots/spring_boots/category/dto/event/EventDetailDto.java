package com.spring_boots.spring_boots.category.dto.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@Builder
public class EventDetailDto {

  private Long id;
  private String eventTitle;
  private String eventContent;
  private String thumbnailImageUrl;
  private List<String> contentImageUrl;
  private LocalDate startDate;
  private LocalDate endDate;
  private Boolean isActive;

}
