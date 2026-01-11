package com.tourney;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.common",
        "com.tourney"
})
@EntityScan({
        "com.common.domain",
        "com.tourney.domain"
})
@EnableJpaRepositories({
        "com.tourney.repository"
})
public class TourneyApplication {
    public static void main(String[] args) {
        SpringApplication.run(TourneyApplication.class, args);
    }

    @Bean
    ApplicationRunner printJwtSecret(Environment env) {
        return args -> {
            System.out.println("security.jwt.secret-base64 = " + env.getProperty("security.jwt.secret-base64"));
        };
    }

}