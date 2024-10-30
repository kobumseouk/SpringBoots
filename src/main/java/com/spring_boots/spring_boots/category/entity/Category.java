package com.spring_boots.spring_boots.category.entity;

import com.spring_boots.spring_boots.common.BaseTimeEntity;
import com.spring_boots.spring_boots.item.entity.Item;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Table(name = "category", indexes = {
        @Index(name = "idx_category_thema", columnList = "category_thema"),     // 카테고리 주제별 조회를 위한 인덱스
        @Index(name = "idx_category_name", columnList = "category_name"),       // 카테고리 이름별 조회를 위한 인덱스
        @Index(name = "idx_display_order", columnList = "display_order")        // 표시 순서별 조회를 위한 인덱스
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Category extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "category_id")
  private Long id;

  @Column(name = "category_thema", nullable = false)
  private String categoryThema;

  @Column(name = "category_name", nullable = false)
  private String categoryName;

  @Column(name = "category_content")
  private String categoryContent;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;  // 카테고리의 표시 순서

  @Column(name = "image_url")
  private String imageUrl;


  // createdAt과 updatedAt은 BaseTimeEntity  상속


  // 1. 생성자를 통한 초기화
  // 2. 빌더 패턴 사용
  // 3. 비즈니스 메서드 구현


}
