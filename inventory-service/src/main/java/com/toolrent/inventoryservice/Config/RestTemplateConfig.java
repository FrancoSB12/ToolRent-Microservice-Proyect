package com.toolrent.inventoryservice.Config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestTemplateConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Agregamos un INTERCEPTOR (un espía) a cada petición saliente
        restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {

            // 1. Buscamos quién está logueado actualmente en este microservicio
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 2. Si es un usuario autenticado con JWT...
            if (authentication instanceof JwtAuthenticationToken jwtToken) {
                // 3. Obtenemos el token "crudo" (eyJhbGciOi...)
                String tokenValue = jwtToken.getToken().getTokenValue();

                // 4. Se lo pegamos a la cabecera de la petición saliente
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue);
            }

            // 5. Dejamos que la petición continúe su camino
            return execution.execute(request, body);
        }));

        return restTemplate;
    }
}
