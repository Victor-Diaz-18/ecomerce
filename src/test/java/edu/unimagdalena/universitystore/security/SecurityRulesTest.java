package edu.unimagdalena.universitystore.security;

import edu.unimagdalena.universitystore.controller.AccountController;
import edu.unimagdalena.universitystore.controller.OrderController;
import edu.unimagdalena.universitystore.controller.ProductController;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.User;
import edu.unimagdalena.universitystore.enums.Role;
import edu.unimagdalena.universitystore.exception.GlobalExceptionHandler;
import edu.unimagdalena.universitystore.mapper.AddressMapper;
import edu.unimagdalena.universitystore.mapper.CustomerMapper;
import edu.unimagdalena.universitystore.mapper.ProductMapper;
import edu.unimagdalena.universitystore.security.config.SecurityConfig;
import edu.unimagdalena.universitystore.security.error.Http401EntryPoint;
import edu.unimagdalena.universitystore.security.error.Http403AccessDenied;
import edu.unimagdalena.universitystore.security.jwt.JwtService;
import edu.unimagdalena.universitystore.security.service.AuthService;
import edu.unimagdalena.universitystore.security.web.AuthController;
import edu.unimagdalena.universitystore.service.AccountService;
import edu.unimagdalena.universitystore.service.ProductService;
import edu.unimagdalena.universitystore.service.PurchaseOrderService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Las reglas de quién puede llamar a qué, con la seguridad real encendida: cada petición pasa por el
 * filtro del token y por SecurityConfig. Los servicios son simulados.
 */
@WebMvcTest(controllers = {OrderController.class, ProductController.class, AccountController.class, AuthController.class})
@Import({SecurityConfig.class, Http401EntryPoint.class, Http403AccessDenied.class, GlobalExceptionHandler.class})
class SecurityRulesTest {
    private static final String ADMIN_TOKEN = "token-de-admin";
    private static final String CUSTOMER_TOKEN = "token-de-cliente";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private PurchaseOrderService orderService;
    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private ProductMapper productMapper;
    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private CustomerMapper customerMapper;
    @MockitoBean
    private AddressMapper addressMapper;
    @MockitoBean
    private AuthService authService;

    private MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder request) {
        when(jwtService.extractUsername(ADMIN_TOKEN)).thenReturn("admin@tienda.com");
        when(userDetailsService.loadUserByUsername("admin@tienda.com"))
                .thenReturn(User.builder().id(1L).email("admin@tienda.com").role(Role.ADMIN).build());
        return request.header("Authorization", "Bearer " + ADMIN_TOKEN);
    }

    // Customer 7
    private MockHttpServletRequestBuilder asCustomer(MockHttpServletRequestBuilder request) {
        when(jwtService.extractUsername(CUSTOMER_TOKEN)).thenReturn("ana@correo.com");
        when(userDetailsService.loadUserByUsername("ana@correo.com"))
                .thenReturn(User.builder().id(2L).email("ana@correo.com").role(Role.CUSTOMER)
                        .customer(Customer.builder().id(7L).build()).build());
        return request.header("Authorization", "Bearer " + CUSTOMER_TOKEN);
    }

    @Test
    void anyoneCanSeeTheCatalog() throws Exception {
        when(productService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products/all")).andExpect(status().isOk());
    }

    @Test
    void withoutLoggingInTheRestOfTheApiAnswers401() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
        mockMvc.perform(get("/api/v1/me/orders")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/products").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService, accountService, productService);
    }

    @Test
    void adminCanUseTheManagementApi() throws Exception {
        when(orderService.findAll()).thenReturn(List.of());

        mockMvc.perform(asAdmin(get("/api/v1/orders"))).andExpect(status().isOk());
    }

    @Test
    void customerCannotUseTheManagementApi() throws Exception {
        mockMvc.perform(asCustomer(get("/api/v1/orders"))).andExpect(status().isForbidden());
        mockMvc.perform(asCustomer(patch("/api/v1/orders/1/ship"))).andExpect(status().isForbidden());
        mockMvc.perform(asCustomer(delete("/api/v1/products/1"))).andExpect(status().isForbidden());

        verifyNoInteractions(orderService, productService);
    }

    @Test
    void customerPortalAlwaysUsesTheCustomerOfTheSession() throws Exception {
        when(accountService.orders(7L)).thenReturn(List.of());

        mockMvc.perform(asCustomer(get("/api/v1/me/orders"))).andExpect(status().isOk());
        mockMvc.perform(asCustomer(patch("/api/v1/me/orders/15/pay"))).andExpect(status().isOk());

        verify(accountService).orders(7L);
        verify(accountService).payOrder(7L, 15L);
    }

    @Test
    void adminHasNoCustomerPortal() throws Exception {
        mockMvc.perform(asAdmin(get("/api/v1/me/orders"))).andExpect(status().isForbidden());

        verifyNoInteractions(accountService);
    }

    @Test
    void expiredOrForgedTokenCountsAsNotLoggedIn() throws Exception {
        when(jwtService.extractUsername("token-malo")).thenThrow(new JwtException("expired"));

        mockMvc.perform(get("/api/v1/orders").header("Authorization", "Bearer token-malo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginIsPublicAndWrongPasswordAnswers401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"ana@correo.com\",\"password\":\"clave-equivocada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Correo o contraseña incorrectos"));
    }
}
