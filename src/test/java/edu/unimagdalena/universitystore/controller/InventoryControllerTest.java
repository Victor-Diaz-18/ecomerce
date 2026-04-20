package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.InventoryDtos;
import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.mapper.InventoryMapper;
import edu.unimagdalena.universitystore.service.InventoryService;
import edu.unimagdalena.universitystore.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@Import(GlobalExceptionHandler.class)
class InventoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService service;

    @MockitoBean
    private InventoryMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateInventory() throws Exception {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .availableStock(10)
                .minimumStock(5)
                .product(Product.builder().id(1L).build())
                .build();

        InventoryDtos.InventoryResponse response =
                new InventoryDtos.InventoryResponse(
                        1L,
                        10,
                        5,
                        1L
                );

        InventoryDtos.UpdateInventoryRequest request =
                new InventoryDtos.UpdateInventoryRequest(
                        10,
                        5
                );

        when(mapper.toEntity(request)).thenReturn(inventory);
        when(service.create(inventory)).thenReturn(inventory);
        when(mapper.toResponse(inventory)).thenReturn(response);

        mockMvc.perform(post("/api/v1/inventories")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableStock").value(10));
    }

    @Test
    void shouldUpdateStock() throws Exception {
        Inventory updated = Inventory.builder()
                .id(1L)
                .availableStock(20)
                .minimumStock(5)
                .product(Product.builder().id(1L).build())
                .build();

        InventoryDtos.InventoryResponse response =
                new InventoryDtos.InventoryResponse(
                        1L,
                        20,
                        5,
                        1L
                );

        InventoryDtos.UpdateInventoryRequest request =
                new InventoryDtos.UpdateInventoryRequest(
                        20,
                        5
                );

        when(service.updateStock(1L, 20)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/inventories/1")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableStock").value(20));
    }

    @Test
    void shouldFindLowStockProducts() throws Exception {
        Inventory inventory = Inventory.builder()
                .id(1L)
                .availableStock(2)
                .minimumStock(5)
                .product(Product.builder().id(1L).name("Mouse").build())
                .build();

        InventoryDtos.LowStockProductResponse response =
                new InventoryDtos.LowStockProductResponse(
                        1L,
                        "Mouse",
                        2,
                        5
                );

        when(service.findLowStockProducts()).thenReturn(List.of(inventory));
        when(mapper.toLowStockResponse(inventory)).thenReturn(response);

        mockMvc.perform(get("/api/v1/inventories/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Mouse"));
    }
}