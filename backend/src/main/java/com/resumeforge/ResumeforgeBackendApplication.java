package com.resumeforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResumeforgeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResumeforgeBackendApplication.class, args);
	}

}
