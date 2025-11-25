package com.banew.cw2025_backend_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class Cw2025BackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(Cw2025BackendApplication.class, args);
	}
}