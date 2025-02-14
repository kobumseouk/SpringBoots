package com.spring_boots.spring_boots.category.entity;

import com.spring_boots.spring_boots.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "event", indexes = {
        @Index(name = "idx_event_title", columnList = "event_title"),       // 이벤트 제목별 조회를 위한 인덱스
        @Index(name = "idx_start_date", columnList = "start_date"),         // 시작일로 조회할 경우를 대비한 인덱스
        @Index(name = "idx_end_date", columnList = "end_date"),             // 종료일로 조회할 경우를 대비한 인덱스
        @Index(name = "idx_is_active", columnList = "is_active")            // 활성화 여부로 조회할 경우를 대비한 인덱스
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "event_id")
  private Long id;

  @Column(name = "event_title", nullable = false)
  private String eventTitle;

  @Column(name = "event_content", columnDefinition = "TEXT", nullable = false)
  private String eventContent;

  @Column(name = "thumbnail_image_url")
  private String thumbnailImageUrl = "";

  @ElementCollection
  @CollectionTable(name = "event_content_images", joinColumns = @JoinColumn(name = "event_id"))
  @Column(name = "content_image_url")
  @Builder.Default
  private List<String> contentImageUrl = new ArrayList<>();  // 빈 리스트로 초기화

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;  // 기본값을 true로 설정, false인 경우 이벤트 글이 사용자에게 보이지 않게 설정

  // 이벤트 상태 설정
  public String getEventStatus() {
    LocalDate now = LocalDate.now();
    if (now.isBefore(this.startDate)) {
      return "예정";
    } else if (now.isAfter(this.endDate)) {
      return "만료";
    } else {
      return "진행중";
    }
  }

  // end_date가 지났는지 확인하고 is_Active를 업데이트하는 메서드 (예정된 이벤트는 표시)
  public void updateActiveStatus() {
    LocalDate now = LocalDate.now();
    this.isActive = now.isBefore(this.endDate) || now.equals(this.endDate);
  }

  // 이벤트 종료일 변경 설정 시 자동으로 상태 업데이트
  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
    updateActiveStatus();
  }

  // 1. 생성자를 통한 초기화
  // 2. 빌더 패턴 사용
  // 3. 비즈니스 메서드 구현
}

