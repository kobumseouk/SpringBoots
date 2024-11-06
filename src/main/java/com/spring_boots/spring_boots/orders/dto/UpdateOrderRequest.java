package com.spring_boots.spring_boots.orders.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateOrderRequest {
    @JsonProperty("shipping_address")
    private String shippingAddress;
    @JsonProperty("recipient_name")
    private String recipientName;
    @JsonProperty("recipient_contact")
    private String recipientContact;
    @JsonProperty("delivery_Message")
    private String deliveryMessage;
}
