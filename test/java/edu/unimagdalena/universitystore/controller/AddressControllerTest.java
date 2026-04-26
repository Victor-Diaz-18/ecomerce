package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.dto.AddressDtos;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.mapper.AddressMapper;
import edu.unimagdalena.universitystore.service.AddressService;
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

@WebMvcTest(AddressController.class)
@Import(GlobalExceptionHandler.class)
class AddressControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AddressService service;

    @MockitoBean
    private AddressMapper mapper;

    @Test
    void shouldCreateAddress() throws Exception {
        AddressDtos.CreateAddressRequest request =
                new AddressDtos.CreateAddressRequest(
                        "Street 10",
                        "Santa Marta",
                        "Colombia",
                        1L
                );

        Address address = Address.builder()
                .id(1L)
                .street("Street 10")
                .city("Santa Marta")
                .country("Colombia")
                .customer(Customer.builder().id(1L).build())
                .build();

        AddressDtos.AddressResponse response =
                new AddressDtos.AddressResponse(
                        1L,
                        "Street 10",
                        "Santa Marta",
                        "Colombia",
                        1L
                );

        when(mapper.toEntity(request)).thenReturn(address);
        when(service.create(address)).thenReturn(address);
        when(mapper.toResponse(address)).thenReturn(response);

        mockMvc.perform(post("/api/v1/addresses")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.street").value("Street 10"));
    }

    @Test
    void shouldFindAddressById() throws Exception {
        Address address = Address.builder()
                .id(1L)
                .street("Street 10")
                .build();

        AddressDtos.AddressResponse response =
                new AddressDtos.AddressResponse(
                        1L,
                        "Street 10",
                        "Santa Marta",
                        "Colombia",
                        1L
                );

        when(service.findById(1L)).thenReturn(address);
        when(mapper.toResponse(address)).thenReturn(response);

        mockMvc.perform(get("/api/v1/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Street 10"));
    }

    @Test
    void shouldDeleteAddress() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/v1/addresses/1"))
                .andExpect(status().isNoContent());
    }
}