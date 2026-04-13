package com.example.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpRequest;

import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // ✅ Skip public endpoints
        if (path.startsWith("/api/v1/users/register") ||
        	    path.startsWith("/api/v1/users/login") ||
        	    path.contains("/oauth2")) {
        	    return chain.filter(exchange);
        	}
        

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            System.out.println("➡️ VALIDATING TOKEN");

            if (!jwtUtil.validateToken(token)) {
                System.out.println("❌ TOKEN INVALID");
                return unauthorized(exchange);
            }

//            System.out.println("✅ TOKEN VALID");

            Object userIdObj = jwtUtil.extractUserId(token);

//            System.out.println("USER ID RAW: " + userIdObj);

            Long userId = Long.parseLong(userIdObj.toString());

//            System.out.println("✅ USER ID: " + userId);

            ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header("X-User-Id", String.valueOf(userId))
                .build();

            return chain.filter(exchange.mutate().request(request).build());

        } catch (Exception e) {
            e.printStackTrace();
            return unauthorized(exchange);
        }
    }	
    

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}