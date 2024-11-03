package com.spring_boots.spring_boots.orders.entity;
import com.spring_boots.spring_boots.common.BaseTimeEntity;
import com.spring_boots.spring_boots.item.entity.Item;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "orderItems", indexes = {
        @Index(name = "idx_orders_id", columnList = "orders_id"),  // Orders와의 조인을 위한 인덱스
        @Index(name = "idx_item_id", columnList = "item_id"),      // Item과의 조인을 위한 인덱스
        @Index(name = "idx_orderItems_created_at", columnList = "created_at") // 날짜별 조회를 위한 인덱스
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItems extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_items_id")
    private Long orderItemsId;

    @ManyToOne
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders orders;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "item_size", nullable = false)
    private Integer itemSize;

    @Column(name = "orderitems_total_price", nullable = false)
    private Integer orderItemsTotalPrice;

    @Column(name = "orderitems_quantity", nullable = false)
    private Integer orderItemsQuantity;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_contact", nullable = false)
    private String recipientContact;

    @Column(name = "delivery_message")
    private String deliveryMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
