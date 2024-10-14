package com.spring_boots.spring_boots.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDto {
    private Long ordersId;
    private LocalDateTime createdAt;
    private int ordersTotalPrice;
    private String orderStatus;
    private String shippingAddress;
    private String recipientName;
    private String recipientContact;
    private int deliveryFee;
    private int quantity;
    private List<OrderItemDetailsDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetailsDto {
        private String itemName;
        private int orderItemsQuantity;
        private int orderItemsTotalPrice;
        private int itemsSize;
        private String itemImage; // 이미지 URL
    }
}
