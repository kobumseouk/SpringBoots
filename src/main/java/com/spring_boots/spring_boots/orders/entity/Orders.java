package com.spring_boots.spring_boots.orders.entity;

import com.spring_boots.spring_boots.common.BaseTimeEntity;
import com.spring_boots.spring_boots.user.domain.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),          // Users와의 조인을 위한 인덱스
        @Index(name = "idx_order_status", columnList = "order_status"), // 주문 상태별 조회를 위한 인덱스
        @Index(name = "idx_is_canceled", columnList = "is_canceled"),   // 취소 여부 조회를 위한 인덱스
        @Index(name = "idx_orders_created_at", columnList = "created_at")      // 날짜별 조회를 위한 인덱스
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orders extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_id")
    private Long ordersId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "orders_total_price", nullable = false)
    private Integer ordersTotalPrice;

    @Column(name = "discount_amount")
    private Integer discountAmount;

    @Column(name = "delivery_fee")
    private Integer deliveryFee;

    @Column(name = "order_status", nullable = false)
    private String orderStatus;

    @Column(name = "is_canceled", nullable = false)
    private Boolean isCanceled;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItems> orderItemsList = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
