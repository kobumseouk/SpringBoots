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
@Table(name = "item")
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
    @CollectionTable(name = "item_keywords", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<OrderItems> orderItems = new ArrayList<>();
}
