package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.OrderDtos;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.mapper.OrderMapper;
import edu.unimagdalena.universitystore.service.PurchaseOrderService;
import edu.unimagdalena.universitystore.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PurchaseOrderService service;

    @MockitoBean
    private OrderMapper mapper;

    @Test
    void shouldCreateOrder() throws Exception {
        OrderDtos.CreateOrderRequest request =
                new OrderDtos.CreateOrderRequest(
                        1L,
                        1L,
                        List.of(new OrderDtos.CreateOrderItemRequest(
                                1L,
                                2
                        ))
                );

        PurchaseOrder order = PurchaseOrder.builder()
                .id(1L)
                .customer(Customer.builder().id(1L).build())
                .address(Address.builder().id(1L).build())
                .status(OrderStatus.CREATED)
                .total(new BigDecimal("100000"))
                .createdAt(LocalDateTime.now())
                .build();

        OrderDtos.OrderResponse response =
                new OrderDtos.OrderResponse(
                        1L,
                        "CREATED",
                        LocalDateTime.now(),
                        1L,
                        List.of(),
                        new BigDecimal("100000")
                );

        when(mapper.toEntity(request)).thenReturn(order);
        when(service.create(order)).thenReturn(order);
        when(mapper.toResponse(order)).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldFindAllOrders() throws Exception {
        PurchaseOrder order = PurchaseOrder.builder()
                .id(1L)
                .status(OrderStatus.CREATED)
                .total(new BigDecimal("100000"))
                .customer(Customer.builder().id(1L).build())
                .build();

        OrderDtos.OrderResponse response =
                new OrderDtos.OrderResponse(
                        1L,
                        "CREATED",
                        LocalDateTime.now(),
                        1L,
                        List.of(),
                        new BigDecimal("100000")
                );

        when(service.findAll()).thenReturn(List.of(order));
        when(mapper.toResponse(order)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }

    @Test
    void shouldFindOrderById() throws Exception {
        PurchaseOrder order = PurchaseOrder.builder()
                .id(1L)
                .status(OrderStatus.CREATED)
                .total(new BigDecimal("100000"))
                .customer(Customer.builder().id(1L).build())
                .build();

        OrderDtos.OrderResponse response =
                new OrderDtos.OrderResponse(
                        1L,
                        "CREATED",
                        LocalDateTime.now(),
                        1L,
                        List.of(),
                        new BigDecimal("100000")
                );

        when(service.findById(1L)).thenReturn(order);
        when(mapper.toResponse(order)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldPayOrder() throws Exception {
        PurchaseOrder paid = PurchaseOrder.builder()
                .id(1L)
                .status(OrderStatus.PAID)
                .total(new BigDecimal("100000"))
                .customer(Customer.builder().id(1L).build())
                .build();

        OrderDtos.OrderResponse response =
                new OrderDtos.OrderResponse(
                        1L,
                        "PAID",
                        LocalDateTime.now(),
                        1L,
                        List.of(),
                        new BigDecimal("100000")
                );

        when(service.payOrder(1L)).thenReturn(paid);
        when(mapper.toResponse(paid)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/orders/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        PurchaseOrder cancelled = PurchaseOrder.builder()
                .id(1L)
                .status(OrderStatus.CANCELLED)
                .total(new BigDecimal("100000"))
                .customer(Customer.builder().id(1L).build())
                .build();

        OrderDtos.OrderResponse response =
                new OrderDtos.OrderResponse(
                        1L,
                        "CANCELLED",
                        LocalDateTime.now(),
                        1L,
                        List.of(),
                        new BigDecimal("100000")
                );

        when(service.cancelOrder(1L)).thenReturn(cancelled);
        when(mapper.toResponse(cancelled)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}