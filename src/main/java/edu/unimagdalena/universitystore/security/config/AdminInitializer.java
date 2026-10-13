package edu.unimagdalena.universitystore.security.config;

import edu.unimagdalena.universitystore.entity.User;
import edu.unimagdalena.universitystore.enums.Role;
import edu.unimagdalena.universitystore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea la cuenta del administrador al arrancar, con el correo y la contraseña de la configuración.
 * No hay registro público de administradores: esta es la única forma de tener uno.
 * Si la cuenta ya existe no se toca, así que cambiar la contraseña en la configuración no la cambia.
 */
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    // Los valores de desarrollo que vienen en application.properties, a la vista de cualquiera en el repositorio
    static final String DEV_JWT_SECRET = "12345678901234567890123456789012";
    static final String DEV_ADMIN_PASSWORD = "admin12345";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.admin.email}")
    private String adminEmail;

    @Value("${security.admin.password}")
    private String adminPassword;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.require-secrets:false}")
    private boolean requireSecrets;

    @Override
    public void run(ApplicationArguments args) {
        // In the cloud, refuse to start with the well-known development values
        if (requireSecrets && (DEV_JWT_SECRET.equals(jwtSecret) || DEV_ADMIN_PASSWORD.equals(adminPassword))) {
            throw new IllegalStateException(
                    "Define las variables JWT_SECRET y ADMIN_PASSWORD: no se puede publicar con los valores de desarrollo");
        }

        String email = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build());
        log.info("Cuenta de administrador creada: {}", email);
    }
}
