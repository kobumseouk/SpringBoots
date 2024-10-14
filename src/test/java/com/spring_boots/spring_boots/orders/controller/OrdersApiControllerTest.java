package com.spring_boots.spring_boots.orders.controller;

import com.spring_boots.spring_boots.config.jwt.impl.JwtProviderImpl;
import com.spring_boots.spring_boots.orders.dto.*;
import com.spring_boots.spring_boots.orders.entity.Orders;
import com.spring_boots.spring_boots.orders.service.OrdersService;
import com.spring_boots.spring_boots.user.domain.UserRole;
import com.spring_boots.spring_boots.user.domain.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
class OrdersApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrdersService ordersService;

    private Users mockUser;

    @BeforeEach
    void setUp() {
        // Mock User 생성
        mockUser = Users.builder()
                .userId(1L)
                .username("testuser")
                .userRealId("user_real_id")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        // SecurityContextHolder에 Mock된 인증 정보를 설정
        // 관리자 테스트 경우 ROLE_ADMIN으로 변경해야함
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        // 공통적으로 사용될 Mock 서비스 데이터 설정
        OrderDto.OrderItemDto mockOrderItem = new OrderDto.OrderItemDto("Test Item", 2, 10000);
        List<OrderDto.OrderItemDto> mockItems = List.of(mockOrderItem);
        OrderDto mockOrder = new OrderDto(1L, LocalDateTime.now(), 20000, "주문완료", "123 Main St", 5000, 2, mockItems);

        // 주문 생성시 반환되는 Mock 객체 설정
        Orders createdOrder = Orders.builder()
                .ordersId(1L)
                .ordersTotalPrice(30000)
                .orderStatus("주문완료")
                .build();

        // OrdersService에 대한 Mock 설정
        Mockito.when(ordersService.getUserOrders(1L)).thenReturn(List.of(mockOrder));
        Mockito.when(ordersService.createOrder(any(OrderRequestDto.class), any(Users.class)))
                .thenReturn(createdOrder);  // 주문 생성시 Orders 반환
    }

    // 사용자 주문 목록 조회 테스트
    @Test
    void getUserOrders() throws Exception {
        // MockMvc로 API 호출 및 응답 출력
        MvcResult result = mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ordersId").value(1L))
                .andExpect(jsonPath("$[0].ordersTotalPrice").value(20000))
                .andExpect(jsonPath("$[0].items[0].itemName").value("Test Item"))
                .andExpect(jsonPath("$[0].items[0].orderItemsQuantity").value(2))
                .andExpect(jsonPath("$[0].items[0].orderItemsTotalPrice").value(10000))
                .andReturn();

        // 응답 본문을 JSON으로 변환하여 예쁘게 출력
        String content = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(mapper.readTree(content));

        System.out.println("정렬된 응답 본문: " + prettyJson);
    }

    // 특정 주문 상세 조회 테스트
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void getOrderDetails() throws Exception {
        OrderDetailsDto.OrderItemDetailsDto mockOrderItemDetails = new OrderDetailsDto.OrderItemDetailsDto(
                "Test Item", 2, 10000, 42, "http://example.com/image.png"
        );
        List<OrderDetailsDto.OrderItemDetailsDto> mockItems = List.of(mockOrderItemDetails);
        OrderDetailsDto mockOrderDetails = new OrderDetailsDto(
                1L, LocalDateTime.now(), 20000, "주문완료", "123 Main St",
                "엘리스", "010-1234-5678", 5000, 2, mockItems
        );

        Mockito.when(ordersService.getOrderDetails(anyLong(), any(Users.class)))
                .thenReturn(Optional.of(mockOrderDetails));

        MvcResult result = mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").value(1L))
                .andExpect(jsonPath("$.ordersTotalPrice").value(20000))
                .andExpect(jsonPath("$.orderStatus").value("주문완료"))
                .andExpect(jsonPath("$.shippingAddress").value("123 Main St"))
                .andExpect(jsonPath("$.recipientName").value("엘리스"))
                .andExpect(jsonPath("$.recipientContact").value("010-1234-5678"))
                .andExpect(jsonPath("$.items[0].itemName").value("Test Item"))
                .andExpect(jsonPath("$.items[0].orderItemsQuantity").value(2))
                .andExpect(jsonPath("$.items[0].orderItemsTotalPrice").value(10000))
                .andExpect(jsonPath("$.items[0].itemsSize").value(42))
                .andExpect(jsonPath("$.items[0].itemImage").value("http://example.com/image.png"))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(mapper.readTree(content));

        System.out.println("정렬된 응답 본문: " + prettyJson);
    }

    // 사용자 주문 수정 테스트
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void updateOrder() throws Exception {
        OrderResponseDto response = new OrderResponseDto(1L, "주문이 성공적으로 수정되었습니다.");
        Mockito.when(ordersService.updateOrder(anyLong(), any(UpdateOrderRequest.class), any(Users.class)))
                .thenReturn(Optional.of(response));

        String updateRequest = "{ \"recipientName\":\"엘리스\", \"shippingAddress\":\"456 Street\", \"recipientContact\":\"010-9876-5432\" }";

        MvcResult result = mockMvc.perform(put("/api/orders/1")
                        .with(csrf())  // CSRF 토큰을 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").value(1L))
                .andExpect(jsonPath("$.status").value("주문이 성공적으로 수정되었습니다."))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(mapper.readTree(content));

        System.out.println("정렬된 응답 본문: " + prettyJson);
    }

    // 사용자 주문 생성 테스트
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createOrder() throws Exception {
        // OrderRequestDto 생성
        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setRecipientName("홍길동");
        orderRequestDto.setRecipientContact("010-1234-5678");
        orderRequestDto.setShippingAddress("서울특별시 강남구");
        orderRequestDto.setDeliveryMessage("빠른 배송 부탁드립니다.");

        OrderRequestDto.OrderItemDto orderItemDto1 = new OrderRequestDto.OrderItemDto();
        orderItemDto1.setItemId(1L);
        orderItemDto1.setItemPrice(10000);
        orderItemDto1.setItemQuantity(2);
        orderRequestDto.setItems(List.of(orderItemDto1));

        // JSON 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(orderRequestDto);

        // MockMvc로 API 호출 및 결과 검증
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))  // CSRF 토큰 추가
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ordersId").value(1L))
                .andExpect(jsonPath("$.status").value("주문이 성공적으로 처리되었습니다."))
                .andReturn();

        // 응답 본문을 JSON으로 변환하여 출력
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(mapper.readTree(content));

        System.out.println("정렬된 응답 본문: " + prettyJson);
    }

    // 사용자 주문 취소 테스트
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void cancelOrder() throws Exception {
        OrderResponseDto response = new OrderResponseDto(1L, "주문이 성공적으로 취소되었습니다.");
        Mockito.when(ordersService.cancelOrder(anyLong(), any(Users.class)))
                .thenReturn(Optional.of(response));

        MvcResult result = mockMvc.perform(delete("/api/orders/1")
                        .with(csrf()))  // CSRF 토큰을 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").exists())
                .andExpect(jsonPath("$.status").value("주문이 성공적으로 취소되었습니다."))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(mapper.readTree(content));

        System.out.println("정렬된 응답 본문: " + prettyJson);
    }

    // 관리자 모든 주문 조회 테스트
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllOrders() throws Exception {
        OrderDto mockOrder = new OrderDto(1L, LocalDateTime.now(), 20000, "주문완료", "123 Main St", 5000, 2, List.of());
        Mockito.when(ordersService.getAllOrders()).thenReturn(Collections.singletonList(mockOrder));

        MvcResult result = mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(mapper.readTree(content));

        System.out.println("정렬된 응답 본문: " + prettyJson);
    }

    // 관리자 주문 상태 수정 테스트
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateOrderStatus() throws Exception {
        OrderResponseDto response = new OrderResponseDto(1L, "주문 상태가 성공적으로 업데이트되었습니다.");
        Mockito.when(ordersService.updateOrderStatus(anyLong(), any(UpdateOrderStatusRequest.class)))
                .thenReturn(Optional.of(response));

        String updateStatusRequest = "{\"status\":\"SHIPPED\"}";

        MvcResult result = mockMvc.perform(patch("/api/admin/orders/1/status")
                        .with(csrf())  // CSRF 토큰을 추가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateStatusRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").exists())
                .andExpect(jsonPath("$.status").value("주문 상태가 성공적으로 업데이트되었습니다."))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(mapper.readTree(content));

        System.out.println("정렬된 응답 본문: " + prettyJson);
    }

    // 관리자 주문 삭제 테스트
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCancelOrder() throws Exception {
        OrderResponseDto response = new OrderResponseDto(1L, "주문이 성공적으로 삭제되었습니다.");
        Mockito.when(ordersService.adminCancelOrder(anyLong()))
                .thenReturn(Optional.of(response));

        MvcResult result = mockMvc.perform(delete("/api/admin/orders/1")
                        .with(csrf()))  // CSRF 토큰을 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordersId").exists())
                .andExpect(jsonPath("$.status").value("주문이 성공적으로 삭제되었습니다."))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String prettyJson = writer.writeValueAsString(mapper.readTree(content));

        System.out.println("정렬된 응답 본문: " + prettyJson);
    }

}
