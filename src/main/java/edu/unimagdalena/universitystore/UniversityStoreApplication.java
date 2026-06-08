package edu.unimagdalena.universitystore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UniversityStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(UniversityStoreApplication.class, args);
	}

}