package com.spring_boots.spring_boots.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long ordersId;
    private LocalDateTime createdAt;
    private int ordersTotalPrice;
    private String orderStatus;
    private String shippingAddress;
    private int deliveryFee;
    private int quantity;
    private List<OrderItemDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private String itemName;
        private int orderItemsQuantity;
        private int orderItemsTotalPrice;
    }
}
