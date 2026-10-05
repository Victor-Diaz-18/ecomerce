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
                        "Customer Name",
                        1L,
                        "Street, City, Country",
                        List.of(),
                        new BigDecimal("100000")
                );

        when(service.create(any(OrderDtos.CreateOrderRequest.class))).thenReturn(response);

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
                        "Customer Name",
                        1L,
                        "Street, City, Country",
                        List.of(),
                        new BigDecimal("100000")
                );

        when(service.findAll()).thenReturn(List.of(response));
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
                        "Customer Name",
                        1L,
                        "Street, City, Country",
                        List.of(),
                        new BigDecimal("100000")
                );

        when(service.findById(1L)).thenReturn(response);
        when(mapper.toResponse(order)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldPayOrder() throws Exception {
        OrderDtos.OrderResponse response =
                new OrderDtos.OrderResponse(
                        1L,
                        "PAID",
                        LocalDateTime.now(),
                        1L,
                        "Customer Name",
                        1L,
                        "Street, City, Country",
                        List.of(),
                        new BigDecimal("100000")
                );

        when(service.payOrder(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/orders/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        OrderDtos.OrderResponse response =
                new OrderDtos.OrderResponse(
                        1L,
                        "CANCELLED",
                        LocalDateTime.now(),
                        1L,
                        "Customer Name",
                        1L,
                        "Street, City, Country",
                        List.of(),
                        new BigDecimal("100000")
                );

        when(service.cancelOrder(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldReturnMonthlyIncomeStats() throws Exception {
        when(service.getMonthlyIncome()).thenReturn(List.of(
                new OrderDtos.MonthlyIncomeResponse(2026, 6, new BigDecimal("1500.00"))
        ));

        mockMvc.perform(get("/api/v1/orders/stats/monthly-income"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(2026))
                .andExpect(jsonPath("$[0].month").value(6))
                .andExpect(jsonPath("$[0].totalIncome").value(1500.00));
    }

    @Test
    void shouldReturnBestSellingStats() throws Exception {
        when(service.getBestSellingProducts()).thenReturn(List.of(
                new OrderDtos.BestSellingProductResponse("Camisa", 5L)
        ));

        mockMvc.perform(get("/api/v1/orders/stats/best-selling"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Camisa"))
                .andExpect(jsonPath("$[0].totalSold").value(5));
    }

    @Test
    void shouldReturnTopCustomersStats() throws Exception {
        when(service.getTopCustomers()).thenReturn(List.of(
                new OrderDtos.TopCustomerResponse("Ana", 2L)
        ));

        mockMvc.perform(get("/api/v1/orders/stats/top-customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("Ana"))
                .andExpect(jsonPath("$[0].totalOrders").value(2));
    }

    @Test
    void shouldReturnIncomeByStatusStats() throws Exception {
        when(service.getIncomeByStatus()).thenReturn(List.of(
                new OrderDtos.IncomeByStatusResponse("CANCELLED", new BigDecimal("500"))
        ));

        mockMvc.perform(get("/api/v1/orders/stats/income-by-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$[0].totalIncome").value(500));
    }
}