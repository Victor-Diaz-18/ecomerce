package edu.unimagdalena.universitystore.security.service;

import edu.unimagdalena.universitystore.dto.AuthDtos.AuthResponse;
import edu.unimagdalena.universitystore.dto.AuthDtos.LoginRequest;
import edu.unimagdalena.universitystore.dto.AuthDtos.RegisterRequest;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.User;
import edu.unimagdalena.universitystore.enums.CustomerStatus;
import edu.unimagdalena.universitystore.enums.Role;
import edu.unimagdalena.universitystore.exception.ConflictException;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.repository.UserRepository;
import edu.unimagdalena.universitystore.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /** Registro público: siempre crea un cliente, nunca un administrador. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        // Also taken if a customer file already uses it: letting someone claim it just by knowing
        // the email would hand them that customer's orders and addresses
        if (userRepository.existsByEmailIgnoreCase(email) || customerRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Email already registered: " + email);
        }

        Customer customer = customerRepository.save(Customer.builder()
                .name(request.name().trim())
                .email(email)
                .status(CustomerStatus.ACTIVE)
                .build());

        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .customer(customer)
                .build());

        return buildResponse(user);
    }

    /** Lanza AuthenticationException si el correo o la contraseña no coinciden. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        return buildResponse(userRepository.findByEmailIgnoreCase(email).orElseThrow());
    }

    private AuthResponse buildResponse(User user) {
        Customer customer = user.getCustomer();
        String token = jwtService.generateToken(user, Map.of("role", user.getRole().name()));

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole().name(),
                customer != null ? customer.getName() : "Administrador",
                customer != null ? customer.getId() : null);
    }
}
