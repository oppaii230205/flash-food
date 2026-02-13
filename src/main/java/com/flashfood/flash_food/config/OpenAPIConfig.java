package com.flashfood.flash_food.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation
 */
@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI flashFoodOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flash-Food API")
                        .description("API for Flash-Food - Food Rescue Application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Flash-Food Team")
                                .email("support@flashfood.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
