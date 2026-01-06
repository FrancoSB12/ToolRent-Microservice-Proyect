package com.toolrent.gatewayservice.Config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/public/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                )
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthConverter() {
        ReactiveJwtAuthenticationConverter converter =
                new ReactiveJwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            Map<String, Object> realmAccess =
                    (Map<String, Object>) jwt.getClaims().get("realm_access");

            if (realmAccess != null && realmAccess.get("roles") instanceof List<?>) {
                List<?> roles = (List<?>) realmAccess.get("roles");
                roles.forEach(r ->
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + r))
                );
            }

            return Flux.fromIterable(authorities);
        });

        return converter;
    }

    @Bean
    public GlobalFilter rolesToHeadersFilter() {
        return (exchange, chain) ->
                exchange.getPrincipal()
                        .cast(JwtAuthenticationToken.class)
                        .flatMap(auth -> {
                            String roles = auth.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .map(r -> r.replace("ROLE_", ""))
                                    .collect(Collectors.joining(","));

                            String userId = auth.getToken().getClaimAsString("preferred_username");

                            ServerHttpRequest request = exchange.getRequest()
                                    .mutate()
                                    .header("X-User-Roles", roles)
                                    .header("X-User-Id", userId)
                                    .build();

                            return chain.filter(exchange.mutate().request(request).build());
                        });
    }
}
