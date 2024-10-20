package com.spring_boots.spring_boots.category.dto.event;

import lombok.*;

import java.time.LocalDate;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {
  private Long id;

  private String eventTitle;
  private String thumbnailImageUrl;
  private LocalDate startDate;
  private LocalDate endDate;
  private String status;  // 예정, 진행 중, 만료 상태 구분
}
