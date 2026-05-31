package com.example.studentmanagement.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studentManagementOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Student Management API")
                .version("1.0.0")
                .description("Public course list, public registration, and admin-protected student CRUD + Excel export.")
                .license(new License().name("Course project")));
    }
}
