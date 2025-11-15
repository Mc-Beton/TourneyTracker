package com.tourney.user_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiConfig() {
        Server devServer = new Server()
                .url("http://localhost:8081")
                .description("Serwer deweloperski");

        Contact contact = new Contact()
                .name("Tournament Tracker Team")
                .email("contact@tourneytracker.com")
                .url("https://tourneytracker.com");

        License license = new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("Tournament Tracker API")
                .version("1.0")
                .description("API do zarządzania turniejami i użytkownikami w Tournament Tracker")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}