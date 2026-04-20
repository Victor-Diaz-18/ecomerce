package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.CustomerDtos;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.enums.CustomerStatus;
import edu.unimagdalena.universitystore.mapper.CustomerMapper;
import edu.unimagdalena.universitystore.service.CustomerService;
import edu.unimagdalena.universitystore.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService service;

    @MockitoBean
    private CustomerMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateCustomer() throws Exception {
        CustomerDtos.CreateCustomerRequest request =
                new CustomerDtos.CreateCustomerRequest(
                        "Juan Perez",
                        "juan@test.com"
                );

        Customer customer = Customer.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan@test.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        CustomerDtos.CustomerResponse response =
                new CustomerDtos.CustomerResponse(
                        1L,
                        "Juan Perez",
                        "juan@test.com",
                        CustomerStatus.ACTIVE
                );

        when(mapper.toEntity(request)).thenReturn(customer);
        when(service.create(customer)).thenReturn(customer);
        when(mapper.toResponse(customer)).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    void shouldFindAllCustomers() throws Exception {
        Customer customer = Customer.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan@test.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        CustomerDtos.CustomerResponse response =
                new CustomerDtos.CustomerResponse(
                        1L,
                        "Juan Perez",
                        "juan@test.com",
                        CustomerStatus.ACTIVE
                );

        when(service.findAll()).thenReturn(List.of(customer));
        when(mapper.toResponse(customer)).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("juan@test.com"));
    }

    @Test
    void shouldFindCustomerById() throws Exception {
        Customer customer =  Customer.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan@test.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        CustomerDtos.CustomerResponse response =
                new CustomerDtos.CustomerResponse(
                        1L,
                        "Juan Perez",
                        "juan@test.com",
                        CustomerStatus.ACTIVE
                );

        when(service.findById(1L)).thenReturn(customer);
        when(mapper.toResponse(customer)).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Juan Perez"));
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        CustomerDtos.UpdateCustomerRequest request =
                new CustomerDtos.UpdateCustomerRequest(
                        "Pedro Alvarez",
                        "pedro@test.com"
                );

        Customer updated = Customer.builder()
                .id(1L)
                .name("Pedro Alvarez")
                .email("pedro@test.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        CustomerDtos.CustomerResponse response =
                new CustomerDtos.CustomerResponse(
                        1L,
                        "Pedro Alvarez",
                        "pedro@test.com",
                        CustomerStatus.ACTIVE
                );

        when(mapper.toEntity(request)).thenReturn(updated);
        when(service.update(1L, updated)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/customers/1")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pedro Alvarez"));
    }

    @Test
    void shouldReturnBadRequestWhenEmailInvalid() throws Exception {
        CustomerDtos.CreateCustomerRequest request =
                new CustomerDtos.CreateCustomerRequest(
                        "Juan Perez",
                        "Invalid Email"
                );

        mockMvc.perform(post("/api/v1/customers")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}