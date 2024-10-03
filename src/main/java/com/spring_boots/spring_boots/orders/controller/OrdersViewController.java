package com.spring_boots.spring_boots.orders.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrdersViewController {

    @GetMapping("/order-summary")
    public String getOrderSummaryPage() {
        // "forward:/order-summary/order-summary"는 resources/static/order-summary/order-summary.html을 반환
        return "forward:/order-summary/order-summary.html";
    }
}
