package edu.unimagdalena.universitystore.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.util.Random;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(SimulationProperties.class)
public class SimulationConfig {
    // Reloj real del sistema y "ruleta" de las devoluciones; como beans para poder controlarlos en los tests
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public Random simulationRandom() {
        return new Random();
    }
}
