package com.flashfood.flash_food.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Global CORS configuration.
 *
 * Allows the Vite dev server (http://localhost:5173) to call the API.
 * In production, replace the allowed origin with the real frontend domain.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origins allowed to make cross-origin requests
        config.setAllowedOrigins(List.of(
                "http://localhost:5173"   // Vite dev server
        ));

        // HTTP methods the browser is allowed to use
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers the browser may send (Authorization is needed for JWT)
        config.setAllowedHeaders(List.of("*"));

        // Allow the browser to read response headers (e.g. custom pagination headers)
        config.setExposedHeaders(List.of("Authorization"));

        // Allow cookies / Authorization header to be forwarded
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour (reduces OPTIONS requests)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
