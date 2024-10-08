package com.spring_boots.spring_boots.orders.controller;

import com.spring_boots.spring_boots.orders.dto.*;
import com.spring_boots.spring_boots.orders.service.OrdersService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrdersApiController {

    private final OrdersService ordersService;
    private static final Logger log = LoggerFactory.getLogger(OrdersApiController.class);


    // 사용자 주문 목록 조회
    @GetMapping("/api/orders")
    public ResponseEntity<?> getUserOrders() {
        try {
            Long userId = 1L; // 임시 데이터로 사용자 ID 지정
            List<OrderDto> orders = ordersService.getUserOrders(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            // 오류 발생 시 로그를 남기고, 500 상태 코드와 오류 메시지를 반환합니다.
            log.error("사용자 주문을 가져오는 도중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("내부 서버 오류가 발생했습니다.");
        }
    }

    // 특정 주문 상세 조회
    @GetMapping("/api/orders/{orders_id}")
    public ResponseEntity<OrderDetailsDto> getOrderDetails(@PathVariable Long orders_id) {
        return ordersService.getOrderDetails(orders_id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 사용자 주문 추가
    /*
    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponseDto> placeOrder(@RequestBody OrderRequestDto request) {
        Long userId = 1L; // 임시 데이터로 사용자 ID 지정
        Orders order = ordersService.placeOrder(request);
        OrderResponseDto response = OrderResponseDto.builder()
                .ordersId(order.getOrdersId())
                .status("주문이 성공적으로 추가되었습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } */

    // 사용자 주문 수정
    @PutMapping("/api/orders/{orders_id}")
    public ResponseEntity<OrderResponseDto> updateOrder(@PathVariable Long orders_id, @RequestBody UpdateOrderRequest request) {
        try {
            return ordersService.updateOrder(orders_id, request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new OrderResponseDto(orders_id, e.getMessage()));
        }
    }

    // 사용자 주문 취소
    @DeleteMapping("/api/orders/{orders_id}")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long orders_id) {
        return ordersService.cancelOrder(orders_id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 관리자 모든 주문 조회
    @GetMapping("/api/admin/orders")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<OrderDto> orders = ordersService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // 관리자 주문 상태 수정
    @PatchMapping("/api/admin/orders/{orders_id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long orders_id, @RequestBody UpdateOrderStatusRequest request) {
        return ordersService.updateOrderStatus(orders_id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 관리자 주문 삭제
    @DeleteMapping("/api/admin/orders/{orders_id}")
    public ResponseEntity<OrderResponseDto> deleteOrder(@PathVariable Long orders_id) {
        return ordersService.cancelOrder(orders_id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
