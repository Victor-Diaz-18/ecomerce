package edu.unimagdalena.universitystore.security.service;

import edu.unimagdalena.universitystore.dto.AuthDtos.*;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByCustomerEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }

        Customer customer = Customer.builder()
                .name(request.name())
                .email(request.email())
                .status(CustomerStatus.ACTIVE)
                .build();
        customerRepository.save(customer);

        Set<Role> roles = request.roles() != null && !request.roles().isEmpty()
                ? request.roles()
                : Set.of(Role.USER);

        User user = User.builder()
                .password(passwordEncoder.encode(request.password()))
                .roles(roles)
                .customer(customer)
                .build();
        userRepository.save(user);

        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByCustomerEmail(request.email()).orElseThrow();
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(user, Map.of("roles", getRoleNames(user)));
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }

    private Set<String> getRoleNames(User user) {
        return user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toSet());
    }
}