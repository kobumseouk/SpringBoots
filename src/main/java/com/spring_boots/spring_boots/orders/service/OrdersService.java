package com.spring_boots.spring_boots.orders.service;

import com.spring_boots.spring_boots.common.config.error.BadRequestException;
import com.spring_boots.spring_boots.item.entity.Item;
import com.spring_boots.spring_boots.item.repository.ItemRepository;
import com.spring_boots.spring_boots.orders.dto.*;
import com.spring_boots.spring_boots.orders.entity.OrderItems;
import com.spring_boots.spring_boots.orders.entity.Orders;
import com.spring_boots.spring_boots.orders.repository.OrderItemsRepository;
import com.spring_boots.spring_boots.orders.repository.OrdersRepository;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.domain.UsersInfo;
import com.spring_boots.spring_boots.user.dto.UserDto;
import com.spring_boots.spring_boots.user.repository.UserInfoRepository;
import com.spring_boots.spring_boots.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrdersService {
    //private static final Logger log = LoggerFactory.getLogger(OrdersService.class);
    private final OrdersRepository ordersRepository;

    private final ItemRepository itemRepository;
    private final OrderItemsRepository orderItemsRepository;

    private final UserService userService;

    private final UserInfoRepository usersInfoRepository;

    // 사용자 주문 목록 조회
    /*
    public List<OrderDto> getUserOrders(Long userId) {
        List<Orders> orders = ordersRepository.findAll(); // 나중에 userId 필터 적용 필요
        return orders.stream()
                .filter(order -> order.getUser() != null &&
                        order.getUser().getUserId().equals(userId) &&
                        !order.getIsCanceled()) // isCanceled가 false인 경우만 필터링
                .map(this::convertToOrderDto)
                .collect(Collectors.toList());
    }*/

    // 사용자 주문 목록 조회
    public List<OrderDto> getUserOrders(Long userId) {
        // userId로 필터링된 Orders를 조회
        List<Orders> orders = ordersRepository.findByUser_UserIdAndIsCanceledFalse(userId);

        return orders.stream()
                .map(this::convertToOrderDto)
                .collect(Collectors.toList());
    }


    // 특정 주문 상세 조회
    public Optional<OrderDetailsDto> getOrderDetails(Long ordersId, UserDto currentUser) {
        return ordersRepository.findById(ordersId)
                .filter(order -> !order.getIsCanceled())
                .filter(order -> order.getUser().getUserId().equals(currentUser.getUserId()))  // 사용자 검증 추가
                .map(order -> {
                    List<OrderItems> orderItemsList = orderItemsRepository.findByOrders(order);

                    List<OrderDetailsDto.OrderItemDetailsDto> orderItemDetailsDtos = orderItemsList.stream()
                            .map(item -> new OrderDetailsDto.OrderItemDetailsDto(
                                    item.getItem().getItemName(),
                                    item.getOrderItemsQuantity(),
                                    item.getOrderItemsTotalPrice(),
                                    item.getItemSize(),
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




    // 사용자 주문 추가
    @Transactional
    public Orders createOrder(OrderRequestDto request, UserDto currentUser) {
        // UserDto를 Users 엔티티로 변환
        Users userEntity = userService.getUserEntityByDto(currentUser);


        // 배송 정보가 없는 경우 users_info 테이블에 추가
        UsersInfo existingInfo = usersInfoRepository.findByUsers_UserId(currentUser.getUserId()).orElse(null);
        if (existingInfo == null) {
            // 배송 정보를 새로 저장
            String fullAddress = request.getShippingAddress();
            String[] addressParts = fullAddress.split(",", 2); // 쉼표를 기준으로 도로명 주소와 상세 주소 분리

            UsersInfo newInfo = UsersInfo.builder()
                    .users(userEntity)  // FK 설정
                    .streetAddress(addressParts.length > 0 ? addressParts[0].trim() : "") // 첫 번째 부분은 도로명 주소
                    .detailedAddress(addressParts.length > 1 ? addressParts[1].trim() : "") // 두 번째 부분은 상세 주소 (없을 수도 있음)
                    .phone(request.getRecipientContact()) // 연락처
                    .build();

            usersInfoRepository.save(newInfo);
        }



        // 필수 필드 유효성 검사
        if (request.getRecipientName().isEmpty() || request.getShippingAddress().isEmpty()) {
            throw new BadRequestException("INVALID_ORDER_REQUEST", "수취인 정보 또는 배송 주소가 누락되었습니다.");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("INVALID_ORDER_REQUEST", "주문할 상품이 없습니다.");
        }

        // 주문의 총 수량 및 총 가격 계산
        int totalQuantity = request.getItems().stream()
                .mapToInt(OrderRequestDto.OrderItemDto::getItemQuantity)
                .sum();

        int totalPrice = request.getItems().stream()
                .mapToInt(item -> item.getItemPrice() * item.getItemQuantity())
                .sum();

        // Orders 엔티티 생성 및 저장
        Orders order = Orders.builder()
                .user(userEntity) // 현재 로그인된 사용자
                .quantity(totalQuantity)
                .ordersTotalPrice(totalPrice)
                .orderStatus("주문완료") // 주문 상태는 기본적으로 '주문완료'
                .isCanceled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Orders savedOrder = ordersRepository.save(order);

        // OrderItems 엔티티 생성 및 저장
        List<OrderItems> orderItemsList = request.getItems().stream()
                .map(itemDto -> {
                    Item item = itemRepository.findById(itemDto.getItemId())
                            .orElseThrow(() -> new BadRequestException("ITEM_NOT_FOUND", "해당 ID의 상품을 찾을 수 없습니다: " + itemDto.getItemId()));

                    return OrderItems.builder()
                            .orders(savedOrder)
                            .item(item)
                            .itemSize(itemDto.getItemSize()) // itemSize 매핑 추가
                            .orderItemsQuantity(itemDto.getItemQuantity())
                            .orderItemsTotalPrice(itemDto.getItemPrice() * itemDto.getItemQuantity())
                            .shippingAddress(request.getShippingAddress())
                            .recipientName(request.getRecipientName())
                            .recipientContact(request.getRecipientContact())
                            .deliveryMessage(request.getDeliveryMessage())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());

        // OrderItems 저장
        orderItemsRepository.saveAll(orderItemsList);

        // OrderItems와 Orders 간의 관계를 설정
        savedOrder.setOrderItemsList(orderItemsList);

        return savedOrder;
    }

    // 사용자 주문 수정
    @Transactional
    public Optional<OrderResponseDto> updateOrder(Long ordersId, UpdateOrderRequest request, UserDto currentUser) {
        return ordersRepository.findById(ordersId).map(order -> {
            // 주문 소유자가 현재 사용자와 일치하는지 확인
            if (!order.getUser().getUserId().equals(currentUser.getUserId())) {
                throw new IllegalStateException("사용자가 이 주문을 수정할 권한이 없습니다.");
            }

            // 배송이 시작되지 않은 경우에만 수정 가능
            if ("주문완료".equals(order.getOrderStatus())) {
                orderItemsRepository.findByOrders(order).forEach(orderItem -> {
                    // 필수 필드 확인 및 업데이트
                    if (request.getRecipientContact() == null || request.getRecipientName() == null || request.getShippingAddress() == null) {
                        throw new IllegalArgumentException("shippingAddress, recipientName, recipientContact는 필수 입력 값입니다.");
                    }

                    orderItem.setShippingAddress(request.getShippingAddress());
                    orderItem.setRecipientName(request.getRecipientName());
                    orderItem.setRecipientContact(request.getRecipientContact());
                    orderItem.setUpdatedAt(LocalDateTime.now());

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
    public Optional<OrderResponseDto> cancelOrder(Long ordersId, UserDto currentUser) {
        return ordersRepository.findById(ordersId)
                .filter(order -> order.getUser().getUserId().equals(currentUser.getUserId())) // 주문 소유자 확인
                .map(order -> {
                    if (!order.getIsCanceled()) {
                        order.setIsCanceled(true);
                        order.setOrderStatus("주문취소");
                        order.setUpdatedAt(LocalDateTime.now());
                        ordersRepository.save(order);
                        return new OrderResponseDto(order.getOrdersId(), "주문이 성공적으로 취소되었습니다.");
                    } else {
                        throw new IllegalStateException("이미 취소된 주문입니다.");
                    }
                });
    }

    //관리자 주문 취소
    public Optional<OrderResponseDto> adminCancelOrder(Long ordersId) {
        return ordersRepository.findById(ordersId).map(order -> {
            if (!order.getIsCanceled()) {
                order.setIsCanceled(true);
                order.setUpdatedAt(LocalDateTime.now());
                ordersRepository.save(order);
                return new OrderResponseDto(order.getOrdersId(), "주문이 관리자로 인해 성공적으로 삭제되었습니다.");
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
        List<Orders> orders = ordersRepository.findByIsCanceledFalse();
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

}
