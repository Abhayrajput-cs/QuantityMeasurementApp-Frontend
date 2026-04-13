package com.example.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        return http
            .csrf(csrf -> csrf.disable()) // 🔥 FIX
            .authorizeExchange(ex -> ex
                .anyExchange().permitAll() // allow all (JWT handled separately)
            )
            .build();
    }
}