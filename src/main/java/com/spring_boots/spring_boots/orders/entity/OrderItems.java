package com.spring_boots.spring_boots.orders.entity;
import com.spring_boots.spring_boots.common.BaseTimeEntity;
import com.spring_boots.spring_boots.item.entity.Item;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "orderItems")
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
