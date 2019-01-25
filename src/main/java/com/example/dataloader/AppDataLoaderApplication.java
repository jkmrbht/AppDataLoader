package com.example.dataloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages="com.example.dataloader")
public class AppDataLoaderApplication {
	public static void main(String[] args) {
		SpringApplication.run(AppDataLoaderApplication.class, args);
	}
}