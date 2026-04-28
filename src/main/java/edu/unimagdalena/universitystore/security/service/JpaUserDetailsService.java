package edu.unimagdalena.universitystore.security.service;

import edu.unimagdalena.universitystore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = repo.findByCustomerEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        var authorities = user.getRoles().stream()
                .map(Enum::name)
                .map( r -> "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}