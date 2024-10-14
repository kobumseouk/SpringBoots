package com.spring_boots.spring_boots.orders.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OrderRequestDto {
    private Long userId;
    private String shippingAddress;
    private String recipientName;
    private String recipientContact;
    private String deliveryMessage;
    private List<OrderItemDto> items;  // List of items in the cart

    @Data
    @NoArgsConstructor
    public static class OrderItemDto {
        private Long itemId;
        private int itemQuantity;
        private int itemSize;
        private int itemPrice;
    }
}
