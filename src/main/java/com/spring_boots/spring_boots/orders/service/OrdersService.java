package com.spring_boots.spring_boots.orders.service;

import com.spring_boots.spring_boots.orders.dto.*;
import com.spring_boots.spring_boots.orders.entity.OrderItems;
import com.spring_boots.spring_boots.orders.entity.Orders;
import com.spring_boots.spring_boots.orders.repository.OrderItemsRepository;
import com.spring_boots.spring_boots.orders.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;

    // 사용자 주문 목록 조회
    public List<OrderDto> getUserOrders(Long userId) {
        List<Orders> orders = ordersRepository.findAll(); // 나중에 userId 필터 적용 필요
        return orders.stream()
                .filter(order -> order.getUser() != null && order.getUser().getUserId().equals(userId))
                .map(this::convertToOrderDto)
                .collect(Collectors.toList());
    }

    // 특정 주문 상세 조회
    public Optional<OrderDetailsDto> getOrderDetails(Long ordersId) {
        return ordersRepository.findById(ordersId).map(order -> {
            List<OrderItems> orderItemsList = orderItemsRepository.findByOrders(order);

            List<OrderDetailsDto.OrderItemDetailsDto> orderItemDetailsDtos = orderItemsList.stream()
                    .map(item -> new OrderDetailsDto.OrderItemDetailsDto(
                            item.getItem().getItemName(),
                            item.getOrderItemsQuantity(),
                            item.getOrderItemsTotalPrice(),
                            item.getItem().getImageUrl() // 이미지 URL 가져오기
                    )).collect(Collectors.toList());

            return new OrderDetailsDto(
                    order.getOrdersId(),
                    order.getCreatedAt(),
                    order.getOrdersTotalPrice(),
                    orderItemsList.isEmpty() ? "대기 중" : orderItemsList.get(0).getOrderStatus(),
                    orderItemsList.isEmpty() ? null : orderItemsList.get(0).getShippingAddress(),
                    orderItemsList.isEmpty() ? null : orderItemsList.get(0).getRecipientName(),
                    orderItemsList.isEmpty() ? null : orderItemsList.get(0).getRecipientContact(),
                    order.getDeliveryFee() != null ? order.getDeliveryFee() : 0,
                    order.getQuantity(),
                    orderItemDetailsDtos
            );
        });
    }

    // 사용자 주문 추가
    public Orders placeOrder(PlaceOrderRequest request) {
        Orders order = Orders.builder()
                .user(null) // 사용자 정보 설정 필요 (예: userId 이용)
                .quantity(1) // 하드코딩된 값, 나중에 장바구니 정보 반영 필요
                .ordersTotalPrice(10000) // 하드코딩된 값, 나중에 계산된 총 금액 반영 필요
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return ordersRepository.save(order);
    }

    // 사용자 주문 수정
    public Optional<OrderResponseDto> updateOrder(Long ordersId, UpdateOrderRequest request) {
        return orderItemsRepository.findById(ordersId).map(orderItem -> {
            orderItem.setShippingAddress(request.getShippingAddress());
            orderItem.setRecipientName(request.getRecipientName());
            orderItem.setRecipientContact(request.getRecipientContact());
            orderItem.setUpdatedAt(LocalDateTime.now());
            orderItemsRepository.save(orderItem);
            return OrderResponseDto.builder()
                    .ordersId(orderItem.getOrders().getOrdersId())
                    .status("주문이 성공적으로 수정되었습니다.")
                    .ordersTotalPrice(orderItem.getOrders().getOrdersTotalPrice())
                    .build();
        });
    }

    // 사용자 주문 취소
    public Optional<OrderResponseDto> cancelOrder(Long ordersId) {
        return orderItemsRepository.findById(ordersId).map(orderItem -> {
            orderItem.setIsCanceled(true);
            orderItem.setUpdatedAt(LocalDateTime.now());
            orderItemsRepository.save(orderItem);
            return OrderResponseDto.builder()
                    .ordersId(orderItem.getOrders().getOrdersId())
                    .status("주문이 성공적으로 취소되었습니다.")
                    .ordersTotalPrice(orderItem.getOrders().getOrdersTotalPrice())
                    .build();
        });
    }

    // 관리자 주문 상태 수정
    public Optional<OrderResponseDto> updateOrderStatus(Long ordersId, UpdateOrderStatusRequest request) {
        return orderItemsRepository.findById(ordersId).map(orderItem -> {
            orderItem.setOrderStatus(request.getOrderStatus());
            orderItem.setUpdatedAt(LocalDateTime.now());
            orderItemsRepository.save(orderItem);
            return OrderResponseDto.builder()
                    .ordersId(orderItem.getOrders().getOrdersId())
                    .status("주문 상태가 성공적으로 수정되었습니다.")
                    .ordersTotalPrice(orderItem.getOrders().getOrdersTotalPrice())
                    .build();
        });
    }

    // 관리자 모든 주문 조회
    public List<OrderDto> getAllOrders() {
        List<Orders> orders = ordersRepository.findAll();
        return orders.stream().map(this::convertToOrderDto).collect(Collectors.toList());
    }

    // DTO 변환 메서드
    private OrderDto convertToOrderDto(Orders orders) {
        List<OrderItems> orderItemsList = orderItemsRepository.findByOrders(orders);

        List<OrderDto.OrderItemDto> orderItemDtos = orderItemsList.stream()
                .map(item -> new OrderDto.OrderItemDto(
                        item.getItem().getItemName(),
                        item.getOrderItemsQuantity(),
                        item.getOrderItemsTotalPrice()
                )).collect(Collectors.toList());

        // Orders에서 OrderDto로 변환하면서 연관된 OrderItems 정보를 포함
        return new OrderDto(
                orders.getOrdersId(),
                orders.getCreatedAt(),
                orders.getOrdersTotalPrice(),
                "대기 중", // 기본 상태
                orderItemsList.isEmpty() ? null : orderItemsList.get(0).getShippingAddress(), // 첫 번째 OrderItem의 배송지 정보 사용
                orders.getDeliveryFee() != null ? orders.getDeliveryFee() : 0,
                orders.getQuantity(), // 총 수량 포함
                orderItemDtos
        );
    }

    private OrderDetailsDto convertToOrderDetailsDto(Orders orders) {
        return new OrderDetailsDto(
                orders.getOrdersId(),
                orders.getCreatedAt(),
                orders.getOrdersTotalPrice(),
                "대기 중", // 기본 상태
                null, // 배송지 정보는 OrderItems에서 관리
                null, // 수령인 정보는 OrderItems에서 관리
                null, // 수령인 연락처는 OrderItems에서 관리
                orders.getDeliveryFee() != null ? orders.getDeliveryFee() : 0, // null일 경우 기본값 0
                orders.getQuantity(), // 총 수량 포함
                null // OrderItemDetailsDto 리스트 추가 필요
        );
    }
}
