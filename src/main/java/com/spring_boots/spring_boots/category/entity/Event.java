package com.spring_boots.spring_boots.category.entity;

import com.spring_boots.spring_boots.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "event")
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

  @Column(name = "event_content", nullable = false)
  private String eventContent;

  @Column(name = "thumbnail_image_url")
  private String thumbnailImageUrl;

  @Column(name = "content_image_url")
  private List<String> contentImageUrl;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;  // 기본값을 true로 설정, false인 경우 이벤트 글이 사용자에게 보이지 않게 설정


  // end_date가 지났는지 확인하고 is_Active를 업데이트하는 메서드
  public void updateActiveStatus() {
    if (this.endDate != null && LocalDate.now().isAfter(this.endDate)) {
      this.isActive = false;
    }
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

