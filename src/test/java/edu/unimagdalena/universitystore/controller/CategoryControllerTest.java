package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.CategoryDtos;
import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.exception.ConflictException;
import edu.unimagdalena.universitystore.mapper.CategoryMapper;
import edu.unimagdalena.universitystore.security.config.SecurityConfig;
import edu.unimagdalena.universitystore.service.CategoryService;
import edu.unimagdalena.universitystore.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class
})
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService service;

    @MockitoBean
    private CategoryMapper mapper;

    @Test
    void shouldCreateCategory() throws Exception {
        CategoryDtos.CreateCategoryRequest request =
                new CategoryDtos.CreateCategoryRequest("Electronics");

        Category category = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        CategoryDtos.CategoryResponse response =
                new CategoryDtos.CategoryResponse(1L, "Electronics");

        when(mapper.toEntity(request)).thenReturn(category);
        when(service.create(category)).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(response);

        mockMvc.perform(post("/api/v1/categories")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void shouldFindCategoryById() throws Exception {
        Category category = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        CategoryDtos.CategoryResponse response =
                new CategoryDtos.CategoryResponse(1L, "Electronics");

        when(service.findById(1L)).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(response);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void shouldReturnConflictWhenCategoryAlreadyExists() throws Exception {
        CategoryDtos.CreateCategoryRequest request =
                new CategoryDtos.CreateCategoryRequest("Electronics");

        Category category = Category.builder()
                .name("Electronics")
                .build();

        when(mapper.toEntity(request)).thenReturn(category);

        when(service.create(category))
                .thenThrow(new ConflictException("Category already exists"));

        mockMvc.perform(post("/api/v1/categories")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Category already exists"));
    }
}