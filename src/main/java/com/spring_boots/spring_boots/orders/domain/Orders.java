package com.spring_boots.spring_boots.orders.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orders {

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
