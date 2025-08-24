package com.example.JournalWebApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.JournalWebApp.Repository")
public class JournalWebAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(JournalWebAppApplication.class, args);
	}

}
