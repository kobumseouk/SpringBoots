package com.spring_boots.spring_boots.item.entity;

import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.common.BaseTimeEntity;
import com.spring_boots.spring_boots.config.StringListConverter;
import com.spring_boots.spring_boots.orders.entity.OrderItems;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "item", indexes = {
        @Index(name = "idx_category_id", columnList = "category_id"),
        @Index(name = "idx_item_name", columnList = "item_name"),
        @Index(name = "idx_item_price", columnList = "item_price"),
        @Index(name = "idx_item_created_at", columnList = "createdAt"),
        @Index(name = "idx_item_quantity", columnList = "item_quantity"),
        @Index(name = "idx_item_name_price", columnList = "item_name, item_price"), // 복합 인덱스
        @Index(name = "idx_category_price", columnList = "category_id, item_price"),  // 카테고리별 가격순
        @Index(name = "idx_category_quantity", columnList = "category_id, item_quantity"),  // 카테고리별 베스트상품
        @Index(name = "idx_category_created", columnList = "category_id, createdAt")        // 카테고리별 최신순
})
@Builder(toBuilder = true)
public class Item extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Category category;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "item_price")
    private Long itemPrice;

    @Column(name = "item_description")
    private String itemDescription;

    @Column(name = "item_maker")
    private String itemMaker;

    @Column(name = "item_color")
    @Convert(converter = StringListConverter.class)
    private List<String> itemColor = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "item_size")
    private Integer itemSize;

    @Column(name = "item_quantity", columnDefinition = "Integer default 0")
    private Integer itemQuantity;  // 총 판매량 (주문 시 업뎃)

    @ElementCollection
    @CollectionTable(
        name = "item_keywords",
        joinColumns = @JoinColumn(name = "item_id"),
        indexes = @Index(name = "idx_keyword", columnList = "keyword")   // 검색 키워드에 대한 인덱스 추가
    )
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<OrderItems> orderItems = new ArrayList<>();
}
