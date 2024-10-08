package com.spring_boots.spring_boots.orders.controller;

import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import com.spring_boots.spring_boots.orders.dto.*;
import com.spring_boots.spring_boots.orders.service.OrdersService;
import com.spring_boots.spring_boots.user.domain.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdersApiController.class)
class OrdersApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrdersService ordersService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;  // JPA 관련 모킹

    @MockBean
    private JwtProviderImpl jwtProviderImpl; // JwtProviderImpl 빈 모킹

    private Users mockUser;

    @BeforeEach
    void setUp() {
        // Mock User 생성
        mockUser = new Users();
        mockUser.setUserId(1L);
        mockUser.setUsername("testuser");

        // SecurityContextHolder에 Mock된 인증 정보를 설정
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    // 사용자 주문 목록 조회 테스트
    @Test
    void getUserOrders() throws Exception {
        // OrderItemDto Mock 데이터 생성
        OrderDto.OrderItemDto mockOrderItem = new OrderDto.OrderItemDto("Test Item", 2, 10000);

        // OrderDto Mock 데이터 생성
        List<OrderDto.OrderItemDto> mockItems = List.of(mockOrderItem);
        OrderDto mockOrder = new OrderDto(1L, LocalDateTime.now(), 20000, "PENDING", "123 Main St", 5000, 2, mockItems);

        // 서비스에서 반환하는 Mock 데이터 설정
        Mockito.when(ordersService.getUserOrders(1L)).thenReturn(List.of(mockOrder));

        // MockMvc로 API 호출 및 응답 출력
        MvcResult result = mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ordersId").value(1L))
                .andExpect(jsonPath("$[0].ordersTotalPrice").value(20000))
                .andExpect(jsonPath("$[0].items[0].itemName").value("Test Item"))
                .andExpect(jsonPath("$[0].items[0].orderitemsQuantity").value(2))
                .andExpect(jsonPath("$[0].items[0].orderitemsTotalPrice").value(10000))
                .andReturn();

        // 응답 본문을 JSON으로 변환하여 예쁘게 출력
        String content = result.getResponse().getContentAsString();

        // ObjectMapper를 이용해 Pretty-Print
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(mapper.readTree(content));

        // JSON 출력
        System.out.println("정렬된 응답 본문: " + prettyJson);
    }






    // 특정 주문 상세 조회 테스트
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getOrderDetails() throws Exception {
        Mockito.when(ordersService.getOrderDetails(anyLong(), any(Users.class)))
                .thenReturn(Optional.of(new OrderDetailsDto()));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").exists());
    }

    // 사용자 주문 수정 테스트
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void updateOrder() throws Exception {
        OrderResponseDto response = new OrderResponseDto(1L, "주문이 성공적으로 수정되었습니다.");
        Mockito.when(ordersService.updateOrder(anyLong(), any(UpdateOrderRequest.class), any(Users.class)))
                .thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientName\":\"Jane\", \"shippingAddress\":\"456 Street\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").exists())
                .andExpect(jsonPath("$.status").value("주문이 성공적으로 수정되었습니다."));
    }

    // 사용자 주문 취소 테스트
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void cancelOrder() throws Exception {
        OrderResponseDto response = new OrderResponseDto(1L, "주문이 성공적으로 취소되었습니다.");
        Mockito.when(ordersService.cancelOrder(anyLong(), any(Users.class)))
                .thenReturn(Optional.of(response));

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").exists())
                .andExpect(jsonPath("$.status").value("주문이 성공적으로 취소되었습니다."));
    }

    // 관리자 모든 주문 조회 테스트
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllOrders() throws Exception {
        Mockito.when(ordersService.getAllOrders()).thenReturn(Collections.singletonList(new OrderDto()));

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // 관리자 주문 상태 수정 테스트
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateOrderStatus() throws Exception {
        OrderResponseDto response = new OrderResponseDto(1L, "주문 상태가 성공적으로 업데이트되었습니다.");
        Mockito.when(ordersService.updateOrderStatus(anyLong(), any(UpdateOrderStatusRequest.class)))
                .thenReturn(Optional.of(response));

        mockMvc.perform(patch("/api/admin/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").exists())
                .andExpect(jsonPath("$.status").value("주문 상태가 성공적으로 업데이트되었습니다."));
    }

    // 관리자 주문 삭제 테스트
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCancelOrder() throws Exception {
        OrderResponseDto response = new OrderResponseDto(1L, "주문이 성공적으로 삭제되었습니다.");
        Mockito.when(ordersService.adminCancelOrder(anyLong()))
                .thenReturn(Optional.of(response));

        mockMvc.perform(delete("/api/admin/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").exists())
                .andExpect(jsonPath("$.status").value("주문이 성공적으로 삭제되었습니다."));
    }
}
