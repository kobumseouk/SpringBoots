package com.spring_boots.spring_boots.orders.service;

import com.spring_boots.spring_boots.common.config.error.BadRequestException;
import com.spring_boots.spring_boots.item.entity.Item;
import com.spring_boots.spring_boots.item.repository.ItemRepository;
import com.spring_boots.spring_boots.orders.dto.*;
import com.spring_boots.spring_boots.orders.entity.OrderItems;
import com.spring_boots.spring_boots.orders.entity.Orders;
import com.spring_boots.spring_boots.orders.repository.OrderItemsRepository;
import com.spring_boots.spring_boots.orders.repository.OrdersRepository;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.domain.Users;
import com.spring_boots.spring_boots.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class OrdersServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private OrderItemsRepository orderItemsRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;  // UserService를 Mock으로 추가

    @InjectMocks
    private OrdersService ordersService;

    private Users mockUser;
    private Orders mockOrder;
    private OrderItems mockOrderItem;

    @BeforeEach
    void setUp() {
        // Mock 설정
        MockitoAnnotations.openMocks(this);

        // Mock 사용자 데이터 생성
        mockUser = Users.builder()
                .userId(1L)
                .username("testuser")
                .userRealId("user_real_id")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        // Mock 주문 데이터 생성
        mockOrder = new Orders();
        mockOrder.setOrdersId(1L);
        mockOrder.setUser(mockUser);
        mockOrder.setOrdersTotalPrice(20000);
        mockOrder.setOrderStatus("주문완료");
        mockOrder.setDeliveryFee(5000);
        mockOrder.setQuantity(2);
        mockOrder.setIsCanceled(false);

        // Mock 아이템 데이터 생성
        Item mockItem = new Item();
        mockItem.setItemName("Test Item");
        mockItem.setItemSize(42);
        mockItem.setImageUrl("http://example.com/image.png");

        // Mock 주문 아이템 데이터 생성
        mockOrderItem = new OrderItems();
        mockOrderItem.setItem(mockItem);
        mockOrderItem.setOrderItemsQuantity(2);
        mockOrderItem.setOrderItemsTotalPrice(10000);
        mockOrderItem.setShippingAddress("123 Main St");
        mockOrderItem.setRecipientName("엘리스");
        mockOrderItem.setRecipientContact("010-1234-5678");
    }
    // 사용자 주문 목록 조회 테스트
    @Test
    void getUserOrders() {
        // Mock 데이터 설정
        when(ordersRepository.findByUser_UserIdAndIsCanceledFalse(1L)).thenReturn(List.of(mockOrder));

        // 서비스 호출
        var result = ordersService.getUserOrders(1L);

        // Assertions
        assertNotNull(result);
        verify(ordersRepository, times(1)).findByUser_UserIdAndIsCanceledFalse(1L);

    }

    // 사용자 주문 목록 조회 실패 테스트
    @Test
    void getUserOrdersFailure() {
        // 주문 목록이 비어있는 경우를 Mock으로 설정
        when(ordersRepository.findByUser_UserIdAndIsCanceledFalse(1L)).thenReturn(List.of());

        // 서비스 호출
        var result = ordersService.getUserOrders(1L);

        // Assertions
        assertTrue(result.isEmpty()); // 결과가 비어 있어야 함
        verify(ordersRepository, times(1)).findByUser_UserIdAndIsCanceledFalse(1L);
    }


    // 특정 주문 상세 조회 테스트
    @Test
    void getOrderDetails() {
        // 리포지토리 Mock 설정
        when(ordersRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));
        when(orderItemsRepository.findByOrders(any(Orders.class))).thenReturn(List.of(mockOrderItem));

        // 서비스 호출
        var result = ordersService.getOrderDetails(1L, mockUser.toUserDto());

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getOrdersId());
        assertEquals(20000, result.get().getOrdersTotalPrice());
        assertEquals("주문완료", result.get().getOrderStatus());
        assertEquals("123 Main St", result.get().getShippingAddress());
        assertEquals("엘리스", result.get().getRecipientName());
        assertEquals("010-1234-5678", result.get().getRecipientContact());
        assertEquals(1, result.get().getItems().size());
        assertEquals("Test Item", result.get().getItems().get(0).getItemName());

        // 리포지토리가 적절히 호출되었는지 검증
        verify(ordersRepository, times(1)).findById(anyLong());
        verify(orderItemsRepository, times(1)).findByOrders(any(Orders.class));
    }

    //특정 주문 상세 조회 실패 테스트
    @Test
    void getOrderDetailsFailure() {
        // 주문을 찾을 수 없는 경우 Mock 설정
        when(ordersRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 서비스 호출
        var result = ordersService.getOrderDetails(1L, mockUser.toUserDto());

        // Assertions
        assertTrue(result.isEmpty()); // 결과가 비어 있어야 함
        verify(ordersRepository, times(1)).findById(anyLong());
    }


    // 사용자 주문 생성 테스트
    @Test
    void createOrder() {
        // 주문 아이템 DTO 생성
        OrderRequestDto.OrderItemDto orderItemDto = new OrderRequestDto.OrderItemDto();
        orderItemDto.setItemId(1L);
        orderItemDto.setItemPrice(10000);
        orderItemDto.setItemQuantity(2);

        // 주문 요청 DTO 생성
        OrderRequestDto request = new OrderRequestDto();
        request.setRecipientName("홍길동");
        request.setRecipientContact("010-1234-5678");
        request.setShippingAddress("서울특별시 강남구");
        request.setDeliveryMessage("빠른 배송 부탁드립니다.");
        request.setItems(List.of(orderItemDto));

        // Mock Item 객체 생성
        Item mockItem = new Item();
        mockItem.setItemName("Test Item");
        mockItem.setItemSize(42);
        mockItem.setImageUrl("http://example.com/image.png");

        // Orders 객체 반환 Mock 설정
        Orders mockOrder = Orders.builder()
                .ordersId(1L)
                .user(mockUser)
                .ordersTotalPrice(20000)
                .quantity(2)
                .orderStatus("주문완료")
                .isCanceled(false)
                .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(mockItem));
        when(ordersRepository.save(any(Orders.class))).thenReturn(mockOrder);

        // 서비스 호출
        Orders result = ordersService.createOrder(request, mockUser.toUserDto());

        // Assertions
        assertNotNull(result);
        assertEquals(1L, result.getOrdersId());
        assertEquals("홍길동", result.getOrderItemsList().get(0).getRecipientName());
        assertEquals("서울특별시 강남구", result.getOrderItemsList().get(0).getShippingAddress());
        assertEquals(20000, result.getOrdersTotalPrice());
        assertEquals(2, result.getQuantity());

        verify(ordersRepository, times(1)).save(any(Orders.class));
        verify(orderItemsRepository, times(1)).saveAll(anyList());
    }

    // 사용자 주문 생성 실패 테스트 (필수 데이터 누락 시)
    @Test
    void createOrderFailure_MissingFields() {
        // 필수 데이터가 없는 주문 요청 DTO 생성
        OrderRequestDto request = new OrderRequestDto();
        request.setRecipientName(""); // 빈 이름
        request.setShippingAddress(""); // 빈 주소

        // 예외 발생을 Mock으로 설정
        assertThrows(BadRequestException.class, () -> {
            ordersService.createOrder(request, mockUser.toUserDto());
        });

        // Repository가 호출되지 않았는지 확인
        verify(ordersRepository, never()).save(any(Orders.class));
        verify(orderItemsRepository, never()).saveAll(anyList());
    }


    // 사용자 주문 수정 테스트
    @Test
    void updateOrder() {
        // Mock 데이터 설정
        Orders mockOrder = new Orders();
        mockOrder.setOrdersId(1L);
        mockOrder.setUser(mockUser);
        mockOrder.setOrderStatus("주문완료");

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setRecipientName("엘리스");
        request.setShippingAddress("456 Street");
        request.setRecipientContact("010-9876-5432");

        // 주문 조회 Mock 설정
        when(ordersRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        // 서비스 호출
        var result = ordersService.updateOrder(1L, request, mockUser.toUserDto());

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("주문이 성공적으로 수정되었습니다.", result.get().getStatus());
        verify(ordersRepository, times(1)).findById(anyLong());
    }

    //사용자 주문 수정 실패 테스트 (권한 없음)
    @Test
    void updateOrderFailure_NotAuthorized() {
        // 다른 사용자가 만든 주문을 Mock으로 설정
        Users otherUser = Users.builder().userId(2L).build();
        Orders mockOrder = new Orders();
        mockOrder.setOrdersId(1L);
        mockOrder.setUser(otherUser); // 다른 사용자로 설정

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setRecipientName("엘리스");
        request.setShippingAddress("456 Street");
        request.setRecipientContact("010-9876-5432");

        // 주문 조회 Mock 설정
        when(ordersRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        // 예외 발생을 기대
        assertThrows(IllegalStateException.class, () -> {
            ordersService.updateOrder(1L, request, mockUser.toUserDto());
        });

        verify(ordersRepository, times(1)).findById(anyLong());
    }



    // 사용자 주문 취소 테스트
    @Test
    void cancelOrder() {
        // Mock 데이터 설정
        Orders mockOrder = new Orders();
        mockOrder.setOrdersId(1L);
        mockOrder.setUser(mockUser);
        mockOrder.setIsCanceled(false);

        when(ordersRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        // 서비스 호출
        var result = ordersService.cancelOrder(1L, mockUser.toUserDto());

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("주문이 성공적으로 취소되었습니다.", result.get().getStatus());
        assertTrue(mockOrder.getIsCanceled());
        verify(ordersRepository, times(1)).findById(anyLong());
    }

    //사용자 주문 취소 실패 테스트 (이미 취소된 주문)
    @Test
    void cancelOrderFailure_AlreadyCanceled() {
        // 이미 취소된 주문 Mock 설정
        Orders mockOrder = new Orders();
        mockOrder.setOrdersId(1L);
        mockOrder.setUser(mockUser);
        mockOrder.setIsCanceled(true); // 이미 취소된 상태

        when(ordersRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        // 예외 발생을 기대
        assertThrows(IllegalStateException.class, () -> {
            ordersService.cancelOrder(1L, mockUser.toUserDto());
        });

        verify(ordersRepository, times(1)).findById(anyLong());
    }


    // 관리자 주문 취소 테스트
    @Test
    void adminCancelOrder() {
        // Mock 데이터 설정
        Orders mockOrder = new Orders();
        mockOrder.setOrdersId(1L);
        mockOrder.setIsCanceled(false);

        when(ordersRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        // 서비스 호출
        var result = ordersService.adminCancelOrder(1L);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("주문이 관리자로 인해 성공적으로 삭제되었습니다.", result.get().getStatus());
        assertTrue(mockOrder.getIsCanceled());
        verify(ordersRepository, times(1)).findById(anyLong());
    }

    //관리자 주문 취소 실패 테스트 (이미 취소된 주문)
    @Test
    void adminCancelOrderFailure_AlreadyCanceled() {
        // 이미 취소된 주문 Mock 설정
        Orders mockOrder = new Orders();
        mockOrder.setOrdersId(1L);
        mockOrder.setIsCanceled(true); // 이미 취소된 상태

        when(ordersRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        // 예외 발생을 기대
        assertThrows(IllegalStateException.class, () -> {
            ordersService.adminCancelOrder(1L);
        });

        verify(ordersRepository, times(1)).findById(anyLong());
    }

    // 관리자 주문 상태 수정 테스트
    @Test
    void updateOrderStatus() {
        // Mock 데이터 설정
        Orders mockOrder = new Orders();
        mockOrder.setOrdersId(1L);
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setOrderStatus("배송완료");

        when(ordersRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));

        // 서비스 호출
        var result = ordersService.updateOrderStatus(1L, request);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("주문 상태가 성공적으로 수정되었습니다.", result.get().getStatus());
        assertEquals("배송완료", mockOrder.getOrderStatus());
        verify(ordersRepository, times(1)).findById(anyLong());
    }

    // 관리자 주문 상태 수정 실패 테스트(주문번호를 찾을 수 없는 경우))
    @Test
    void updateOrderStatusFailure_OrderNotFound() {
        // 주문을 찾을 수 없는 경우 Mock 설정
        when(ordersRepository.findById(anyLong())).thenReturn(Optional.empty());

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setOrderStatus("배송완료");

        // 서비스 호출
        var result = ordersService.updateOrderStatus(1L, request);

        // Assertions
        assertTrue(result.isEmpty()); // 결과가 비어 있어야 함
        verify(ordersRepository, times(1)).findById(anyLong());
    }


    // 관리자 모든 주문 조회 테스트
    @Test
    void getAllOrders() {
        // Mock 데이터 설정 (필드 값들을 설정)
        Orders mockOrder = new Orders();
        mockOrder.setOrdersId(1L);
        mockOrder.setOrdersTotalPrice(20000); // 필수 필드 설정
        mockOrder.setOrderStatus("주문완료");
        mockOrder.setQuantity(2);
        mockOrder.setDeliveryFee(5000);

        // Repository에서 반환할 Mock 데이터 설정
        when(ordersRepository.findByIsCanceledFalse()).thenReturn(List.of(mockOrder));

        // 서비스 호출
        var result = ordersService.getAllOrders();

        // Assertions
        assertNotNull(result);  // 결과가 null이 아님을 확인
        assertEquals(1, result.size());  // 결과 리스트의 크기가 1인지 확인
        assertEquals(20000, result.get(0).getOrdersTotalPrice());  // 총 주문 금액이 올바른지 확인

        // Repository가 적절히 호출되었는지 검증
        verify(ordersRepository, times(1)).findByIsCanceledFalse();
    }

    // 관리자 모든 주문 조회 실패 테스트 (주문이 없는 경우)
    @Test
    void getAllOrdersFailure_NoOrders() {
        // 빈 주문 목록을 반환하는 Mock 설정
        when(ordersRepository.findByIsCanceledFalse()).thenReturn(List.of());

        // 서비스 호출
        var result = ordersService.getAllOrders();

        // Assertions
        assertTrue(result.isEmpty());  // 결과가 비어 있어야 함
        verify(ordersRepository, times(1)).findByIsCanceledFalse();
    }


}
