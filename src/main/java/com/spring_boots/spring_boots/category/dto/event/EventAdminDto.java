package com.spring_boots.spring_boots.category.dto.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EventAdminDto {
  private Long id;
  private String eventTitle;
  private String eventContent;
  private LocalDate startDate;
  private LocalDate endDate;
  private Boolean isActive;   // false인 경우 '만료' 표기
  private String status;  // 진행 예정, 진행 중, 만료 상태 구분
  private LocalDateTime updatedAt;  // 업뎃일 기준으로 이벤트 목록 정렬
}
