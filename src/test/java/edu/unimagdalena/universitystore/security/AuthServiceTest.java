package edu.unimagdalena.universitystore.security;

import edu.unimagdalena.universitystore.dto.AuthDtos.AuthResponse;
import edu.unimagdalena.universitystore.dto.AuthDtos.LoginRequest;
import edu.unimagdalena.universitystore.dto.AuthDtos.RegisterRequest;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.User;
import edu.unimagdalena.universitystore.enums.Role;
import edu.unimagdalena.universitystore.exception.ConflictException;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.repository.UserRepository;
import edu.unimagdalena.universitystore.security.jwt.JwtService;
import edu.unimagdalena.universitystore.security.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesACustomerAccountWithTheEncodedPassword() {
        when(userRepository.existsByEmailIgnoreCase("ana@correo.com")).thenReturn(false);
        when(customerRepository.findByEmail("ana@correo.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any())).thenAnswer(inv -> {
            Customer customer = inv.getArgument(0);
            customer.setId(7L);
            return customer;
        });
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("clave-secreta")).thenReturn("clave-cifrada");
        when(jwtService.generateToken(any(), any())).thenReturn("un-token");

        AuthResponse response = authService.register(new RegisterRequest("Ana", " Ana@Correo.com ", "clave-secreta"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertEquals("ana@correo.com", saved.getValue().getEmail());
        assertEquals("clave-cifrada", saved.getValue().getPassword());
        assertEquals(Role.CUSTOMER, saved.getValue().getRole());
        assertEquals(new AuthResponse("un-token", "ana@correo.com", "CUSTOMER", "Ana", 7L), response);
    }

    @Test
    void registerRejectsAnEmailThatAlreadyHasAnAccount() {
        when(userRepository.existsByEmailIgnoreCase("ana@correo.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> authService.register(new RegisterRequest("Ana", "ana@correo.com", "clave-secreta")));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void registerDoesNotLetAnyoneClaimAnExistingCustomerFile() {
        when(userRepository.existsByEmailIgnoreCase("ana@correo.com")).thenReturn(false);
        when(customerRepository.findByEmail("ana@correo.com")).thenReturn(Optional.of(new Customer()));

        assertThrows(ConflictException.class,
                () -> authService.register(new RegisterRequest("Otra persona", "ana@correo.com", "clave-secreta")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTheAdminWithoutCustomerFile() {
        User admin = User.builder().id(1L).email("admin@tienda.com").role(Role.ADMIN).build();
        when(userRepository.findByEmailIgnoreCase("admin@tienda.com")).thenReturn(Optional.of(admin));
        when(jwtService.generateToken(any(), any())).thenReturn("un-token");

        AuthResponse response = authService.login(new LoginRequest("Admin@Tienda.com", "clave"));

        assertEquals(new AuthResponse("un-token", "admin@tienda.com", "ADMIN", "Administrador", null), response);
    }

    @Test
    void loginWithWrongPasswordGivesNoToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("ana@correo.com", "clave-equivocada")));

        verifyNoInteractions(jwtService);
    }
}
