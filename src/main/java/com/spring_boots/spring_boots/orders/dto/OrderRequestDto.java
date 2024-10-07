package com.spring_boots.spring_boots.orders.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderRequestDto {
    private Long userId;
    private String shippingAddress;
    private String recipientName;
    private String recipientContact;
    private String deliveryMessage;
}
