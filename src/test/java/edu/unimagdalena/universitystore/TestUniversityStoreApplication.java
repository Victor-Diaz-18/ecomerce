package edu.unimagdalena.universitystore;

import org.springframework.boot.SpringApplication;

public class TestUniversityStoreApplication {

	public static void main(String[] args) {
		SpringApplication.from(UniversityStoreApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
