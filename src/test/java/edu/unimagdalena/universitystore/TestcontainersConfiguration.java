package edu.unimagdalena.universitystore;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class TestcontainersConfiguration {

	@Bean
	PostgreSQLContainer<?> postgresContainer() {
		PostgreSQLContainer<?> container =
				new PostgreSQLContainer<>("postgres:16")
						.withDatabaseName("testdb")
						.withUsername("postgres")
						.withPassword("postgres");

		container.start();
		return container;
	}
}