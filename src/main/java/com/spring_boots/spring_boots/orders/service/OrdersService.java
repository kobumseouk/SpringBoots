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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrdersService {
    //private static final Logger log = LoggerFactory.getLogger(OrdersService.class);
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

            // 기본적인 정보를 첫 번째 OrderItems에서 가져오는 방식으로 설정
            String shippingAddress = orderItemsList.isEmpty() ? null : orderItemsList.get(0).getShippingAddress();
            String recipientName = orderItemsList.isEmpty() ? null : orderItemsList.get(0).getRecipientName();
            String recipientContact = orderItemsList.isEmpty() ? null : orderItemsList.get(0).getRecipientContact();

            return new OrderDetailsDto(
                    order.getOrdersId(),
                    order.getCreatedAt(),
                    order.getOrdersTotalPrice(),
                    order.getOrderStatus(),
                    shippingAddress,
                    recipientName,
                    recipientContact,
                    order.getDeliveryFee() != null ? order.getDeliveryFee() : 0,
                    order.getQuantity(),
                    orderItemDetailsDtos
            );
        });
    }


    /*
    // 사용자 주문 추가
    public Orders placeOrder(PlaceOrderRequest request) {
        Orders order = Orders.builder()
                .user(null) // 사용자 정보 설정 필요 (예: userId 이용)
                .quantity(1) // 하드코딩된 값, 나중에 장바구니 정보 반영 필요
                .ordersTotalPrice(10000) // 하드코딩된 값, 나중에 계산된 총 금액 반영 필요
                .orderStatus("Pending") // 기본 상태 설정
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return ordersRepository.save(order);
    } */

    // 사용자 주문 수정
    @Transactional
    public Optional<OrderResponseDto> updateOrder(Long ordersId, UpdateOrderRequest request) {
        Logger log = LoggerFactory.getLogger(OrdersService.class);

        return ordersRepository.findById(ordersId).map(order -> {

            List<OrderItems> orderItemsList = orderItemsRepository.findByOrders(order);

            // 배송이 시작되지 않은 경우에만 수정 가능 (예시: "Pending" 상태일 때만 수정 가능)
            if ("Pending".equals(order.getOrderStatus())) {
                orderItemsList.forEach(orderItem -> {

                    // 필수 필드가 null인 경우 예외 발생
                    if (request.getRecipientContact() == null || request.getRecipientName() == null || request.getShippingAddress() == null) {
                        throw new IllegalArgumentException("shippingAddress, recipientName, recipientContact는 필수 입력 값입니다.");
                    }

                    // OrderItem 업데이트
                    orderItem.setShippingAddress(request.getShippingAddress());
                    orderItem.setRecipientName(request.getRecipientName());
                    orderItem.setRecipientContact(request.getRecipientContact());
                    orderItem.setUpdatedAt(LocalDateTime.now());


                    // 필수 값들이 모두 설정되었는지 다시 확인
                    if (orderItem.getRecipientContact() == null) {
                        throw new IllegalStateException("Order item의 recipientContact가 null로 설정되었습니다.");
                    }

                    orderItemsRepository.save(orderItem);
                });

                // Order 자체 업데이트
                order.setUpdatedAt(LocalDateTime.now());
                ordersRepository.save(order);

                return new OrderResponseDto(order.getOrdersId(), "주문이 성공적으로 수정되었습니다.");
            } else {
                throw new IllegalStateException("배송이 이미 시작되어 주문을 수정할 수 없습니다.");
            }
        });
    }


    // 사용자 주문 취소
    public Optional<OrderResponseDto> cancelOrder(Long ordersId) {
        return ordersRepository.findById(ordersId).map(order -> {
            if (!order.getIsCanceled()) {
                order.setIsCanceled(true);
                order.setUpdatedAt(LocalDateTime.now());
                ordersRepository.save(order);
                return new OrderResponseDto(order.getOrdersId(), "주문이 성공적으로 취소되었습니다.");
            } else {
                throw new IllegalStateException("이미 취소된 주문입니다.");
            }
        });
    }

    // 관리자 주문 상태 수정
    public Optional<OrderResponseDto> updateOrderStatus(Long ordersId, UpdateOrderStatusRequest request) {
        return ordersRepository.findById(ordersId).map(order -> {
            order.setOrderStatus(request.getOrderStatus());
            order.setUpdatedAt(LocalDateTime.now());
            ordersRepository.save(order);
            return new OrderResponseDto(order.getOrdersId(), "주문 상태가 성공적으로 수정되었습니다.");
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

        // OrderItems 리스트에서 기본 정보를 가져오기
        String shippingAddress = orderItemsList.isEmpty() ? null : orderItemsList.get(0).getShippingAddress();

        return new OrderDto(
                orders.getOrdersId(),
                orders.getCreatedAt(),
                orders.getOrdersTotalPrice(),
                orders.getOrderStatus(),
                shippingAddress,
                orders.getDeliveryFee() != null ? orders.getDeliveryFee() : 0,
                orders.getQuantity(),
                orderItemDtos
        );
    }

    /*
    private OrderDetailsDto convertToOrderDetailsDto(Orders orders) {
        List<OrderItems> orderItemsList = orderItemsRepository.findByOrders(orders);

        List<OrderDetailsDto.OrderItemDetailsDto> orderItemDetailsDtos = orderItemsList.stream()
                .map(item -> new OrderDetailsDto.OrderItemDetailsDto(
                        item.getItem().getItemName(),
                        item.getOrderItemsQuantity(),
                        item.getOrderItemsTotalPrice(),
                        item.getItem().getImageUrl()
                )).collect(Collectors.toList());

        // OrderItems 리스트에서 기본 정보를 가져오기
        String shippingAddress = orderItemsList.isEmpty() ? null : orderItemsList.get(0).getShippingAddress();
        String recipientName = orderItemsList.isEmpty() ? null : orderItemsList.get(0).getRecipientName();
        String recipientContact = orderItemsList.isEmpty() ? null : orderItemsList.get(0).getRecipientContact();

        return new OrderDetailsDto(
                orders.getOrdersId(),
                orders.getCreatedAt(),
                orders.getOrdersTotalPrice(),
                orders.getOrderStatus(),
                shippingAddress,
                recipientName,
                recipientContact,
                orders.getDeliveryFee() != null ? orders.getDeliveryFee() : 0,
                orders.getQuantity(),
                orderItemDetailsDtos
        );
    }*/

}
