package com.example.filter;   // SAME package or adjust import

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component   // 🔥 VERY IMPORTANT
public class JwtUtil {

    private final String SECRET = "AbhayRajputSecurityKeyAbhayRajputSecurityKey";

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWT ERROR: " + e.getMessage()); // 🔥 ADD THIS
            return false;
        }
    }

    public Long extractUserId(String token) {
        Object userId = getClaims(token).get("userId");

        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}