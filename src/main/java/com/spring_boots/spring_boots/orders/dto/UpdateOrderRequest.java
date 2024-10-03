package com.spring_boots.spring_boots.orders.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateOrderRequest {
    private String shippingAddress;
    private String recipientName;
    private String recipientContact;
}
