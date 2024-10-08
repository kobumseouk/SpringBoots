package com.spring_boots.spring_boots.orders.service;

import com.spring_boots.spring_boots.item.entity.Item;
import com.spring_boots.spring_boots.orders.dto.*;
import com.spring_boots.spring_boots.orders.entity.OrderItems;
import com.spring_boots.spring_boots.orders.entity.Orders;
import com.spring_boots.spring_boots.orders.repository.OrderItemsRepository;
import com.spring_boots.spring_boots.orders.repository.OrdersRepository;
import com.spring_boots.spring_boots.user.domain.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class OrdersServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private OrderItemsRepository orderItemsRepository;

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
        mockUser = new Users();
        mockUser.setUserId(1L);

        // Mock 주문 데이터 생성
        mockOrder = new Orders();
        mockOrder.setOrdersId(1L);
        mockOrder.setUser(mockUser);
        mockOrder.setOrdersTotalPrice(20000);
        mockOrder.setOrderStatus("주문완료");
        mockOrder.setDeliveryFee(5000);
        mockOrder.setQuantity(2);

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
        when(ordersRepository.findAll()).thenReturn(List.of(new Orders()));

        // 서비스 호출
        var result = ordersService.getUserOrders(1L);

        // Assertions
        assertNotNull(result);
        verify(ordersRepository, times(1)).findAll();
    }

    // 특정 주문 상세 조회 테스트
    @Test
    void getOrderDetails() {
        // 리포지토리 Mock 설정
        when(ordersRepository.findById(anyLong())).thenReturn(Optional.of(mockOrder));
        when(orderItemsRepository.findByOrders(any(Orders.class))).thenReturn(List.of(mockOrderItem));

        // 서비스 호출
        var result = ordersService.getOrderDetails(1L, mockUser);

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
        var result = ordersService.updateOrder(1L, request, mockUser);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("주문이 성공적으로 수정되었습니다.", result.get().getStatus());
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
        var result = ordersService.cancelOrder(1L, mockUser);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("주문이 성공적으로 취소되었습니다.", result.get().getStatus());
        assertTrue(mockOrder.getIsCanceled());
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
        when(ordersRepository.findAll()).thenReturn(List.of(mockOrder));

        // 서비스 호출
        var result = ordersService.getAllOrders();

        // Assertions
        assertNotNull(result);  // 결과가 null이 아님을 확인
        assertEquals(1, result.size());  // 결과 리스트의 크기가 1인지 확인
        assertEquals(20000, result.get(0).getOrdersTotalPrice());  // 총 주문 금액이 올바른지 확인

        // Repository가 적절히 호출되었는지 검증
        verify(ordersRepository, times(1)).findAll();
    }

}
