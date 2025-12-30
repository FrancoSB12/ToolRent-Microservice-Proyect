package com.toolrent.gatewayservice.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // 1. Permitir específicamente tu Frontend
        corsConfig.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));

        // 2. Permitir todos los métodos (GET, POST, PUT, DELETE, OPTIONS)
        corsConfig.addAllowedMethod("*");

        // 3. Permitir todas las cabeceras
        corsConfig.addAllowedHeader("*");

        // 4. Permitir credenciales (necesario a veces para sesiones/cookies)
        corsConfig.setAllowCredentials(true);

        // 5. Aplicar esta configuración a TODAS las rutas (/**)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
