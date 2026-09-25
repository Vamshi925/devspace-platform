package com.devspace.environment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EnvironmentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnvironmentServiceApplication.class, args);
	}

}
