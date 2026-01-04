package com.toolrent.gatewayservice.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        //Allow frontend requests
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost", "http://localhost:5173"));

        //Allow all methods (GET, POST, PUT, DELETE, OPTIONS)
        corsConfig.addAllowedMethod("*");

        //Allow all headers
        corsConfig.addAllowedHeader("*");

        //Allow credentials
        corsConfig.setAllowCredentials(true);

        //Apply to all routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
