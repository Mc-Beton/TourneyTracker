package com.tourney.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.tourney.common",
		"com.tourney.user_service"
})
@EntityScan({
		"com.tourney.common.domain",
		"com.tourney.user_service.domain"
})
@EnableJpaRepositories({
		"com.tourney.common.repository",
		"com.tourney.user_service.repository"
})

public class UserServiceApplication {
	public static void main(String[] args) {

		SpringApplication.run(UserServiceApplication.class, args);
	}
}
