package com.spring_boots.spring_boots.category.dto.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventAdminDto {
  private Long id;
  private String eventTitle;
  private String eventContent;
  private LocalDate startDate;
  private LocalDate endDate;
  private Boolean isActive;   // false인 경우 '만료' 표기

  private LocalDateTime updatedAt;  // 업뎃일 기준으로 이벤트 목록 정렬
}
