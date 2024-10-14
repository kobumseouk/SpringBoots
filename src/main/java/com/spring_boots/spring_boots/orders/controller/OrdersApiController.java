package com.spring_boots.spring_boots.orders.controller;

import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.orders.dto.*;
import com.spring_boots.spring_boots.orders.entity.Orders;
import com.spring_boots.spring_boots.orders.service.OrdersService;
import com.spring_boots.spring_boots.user.domain.Users;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
        // SecurityContext에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((Users) authentication.getPrincipal()).getUserId();  // 필요한 정보만 추출

        log.debug("유저 아이디: {}", userId);

        try {
            List<OrderDto> orders = ordersService.getUserOrders(userId);  // 필요한 정보로만 처리
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("사용자 주문을 가져오는 도중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("내부 서버 오류가 발생했습니다.");
        }
    }


    // 특정 주문 상세 조회
    @GetMapping("/api/orders/{orders_id}")
    public ResponseEntity<OrderDetailsDto> getOrderDetails(@PathVariable Long orders_id, @AuthenticationPrincipal Users currentUser) {
        return ordersService.getOrderDetails(orders_id, currentUser)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("주문번호를 찾을 수 없습니다: " + orders_id));
    }

    // 사용자 주문 추가
    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto request, @AuthenticationPrincipal Users currentUser) {
        log.debug("주문: {}", request);
        Orders order = ordersService.createOrder(request, currentUser);
        log.debug("생성된 주문 ID: {}", order.getOrdersId()); // 주문 ID 확인

        OrderResponseDto response = OrderResponseDto.builder()
                .ordersId(order.getOrdersId())
                .status("주문이 성공적으로 처리되었습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 사용자 주문 수정
    @PutMapping("/api/orders/{orders_id}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long orders_id,
            @RequestBody UpdateOrderRequest request,
            @AuthenticationPrincipal Users currentUser) {

        try {
            // 서비스에 주문 업데이트 요청을 전달, 소유자 검증도 포함됨
            return ordersService.updateOrder(orders_id, request, currentUser)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("주문번호를 찾을 수 없습니다: " + orders_id));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new OrderResponseDto(orders_id, e.getMessage()));
        }
    }


    // 사용자 주문 취소
    @DeleteMapping("/api/orders/{orders_id}")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long orders_id,
            @AuthenticationPrincipal Users authenticatedUser) {

        try {
            return ordersService.cancelOrder(orders_id, authenticatedUser)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException("주문번호를 찾을 수 없습니다: " + orders_id));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new OrderResponseDto(orders_id, e.getMessage()));
        }
    }


    // 관리자 모든 주문 조회
    @GetMapping("/api/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<OrderDto> orders = ordersService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // 관리자 주문 상태 수정
    @PatchMapping("/api/admin/orders/{orders_id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long orders_id, @RequestBody UpdateOrderStatusRequest request) {
        return ordersService.updateOrderStatus(orders_id, request)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("주문번호를 찾을 수 없습니다: " + orders_id));
    }

    // 관리자 주문 삭제
    @DeleteMapping("/api/admin/orders/{orders_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> adminCancelOrder(@PathVariable Long orders_id) {
        return ordersService.adminCancelOrder(orders_id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("주문번호를 찾을 수 없습니다: " + orders_id));
    }
}
