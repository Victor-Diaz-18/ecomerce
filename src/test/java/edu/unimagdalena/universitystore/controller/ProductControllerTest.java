package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.ProductDtos;
import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.mapper.ProductMapper;
import edu.unimagdalena.universitystore.service.ProductService;
import edu.unimagdalena.universitystore.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.math.*;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    @MockitoBean
    private ProductMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateProduct() throws Exception {
        ProductDtos.CreateProductRequest request =
                new ProductDtos.CreateProductRequest(
                        "Mouse",
                        "SKU001",
                        new BigDecimal("50000"),
                        1L
                );

        Product product = Product.builder()
                .id(1L)
                .name("Mouse")
                .sku("SKU001")
                .price(new BigDecimal("50000"))
                .active(true)
                .category(Category.builder().id(1L).build())
                .build();

        ProductDtos.ProductResponse response =
                new ProductDtos.ProductResponse(
                        1L,
                        "Mouse",
                        "SKU001",
                        new BigDecimal("50000"),
                        true,
                        1L
                );

        when(mapper.toEntity(request)).thenReturn(product);
        when(service.create(product)).thenReturn(product);
        when(mapper.toResponse(product)).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU001"));
    }

    @Test
    void shouldFindAllProducts() throws Exception {
        Product product = Product.builder()
                .id(1L)
                .name("Mouse")
                .sku("SKU001")
                .price(new BigDecimal("50000"))
                .active(true)
                .category(Category.builder().id(1L).build())
                .build();

        ProductDtos.ProductResponse response =
                new ProductDtos.ProductResponse(
                        1L,
                        "Mouse",
                        "SKU001",
                        new BigDecimal("50000"),
                        true,
                        1L
                );

        when(service.findAll()).thenReturn(List.of(product));
        when(mapper.toResponse(product)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU001"));
    }

    @Test
    void shouldFindProductById() throws Exception {
        Product product = Product.builder()
                .id(1L)
                .name("Mouse")
                .sku("SKU001")
                .price(new BigDecimal("50000"))
                .active(true)
                .category(Category.builder().id(1L).build())
                .build();

        ProductDtos.ProductResponse response =
                new ProductDtos.ProductResponse(
                        1L,
                        "Mouse",
                        "SKU001",
                        new BigDecimal("50000"),
                        true,
                        1L
                );

        when(service.findById(1L)).thenReturn(product);
        when(mapper.toResponse(product)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU001"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        ProductDtos.UpdateProductRequest request =
                new ProductDtos.UpdateProductRequest(
                        "Keyboard",
                        new BigDecimal("80000"),
                        true
                );

        Product updated = Product.builder()
                .id(1L)
                .sku("SKU002")
                .name("Keyboard")
                .price(new BigDecimal("80000"))
                .active(true)
                .build();

        ProductDtos.ProductResponse response =
                new ProductDtos.ProductResponse(
                        1L,
                        "SKU002",
                        "Keyboard",
                        new BigDecimal("80000"),
                        true,
                        1L
                );

        when(mapper.toEntity(request)).thenReturn(updated);
        when(service.update(1L, updated)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/products/1")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU002"));
    }

    @Test
    void shouldFindProductsByCategory() throws Exception {
        Product product = Product.builder()
                .id(1L)
                .name("Mouse")
                .sku("SKU001")
                .price(new BigDecimal("50000"))
                .active(true)
                .category(Category.builder().id(1L).build())
                .build();

        ProductDtos.ProductResponse response =
                new ProductDtos.ProductResponse(
                        1L,
                        "Mouse",
                        "SKU001",
                        new BigDecimal("50000"),
                        true,
                        1L
                );

        when(service.findByCategory(1L)).thenReturn(List.of(product));
        when(mapper.toResponse(product)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU001"));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(service.findById(99L))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));
    }
}