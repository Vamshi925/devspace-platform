package com.devspace.login.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${devspace.jwt.secret}")
    private String secretKey;

    @Value("${devspace.jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(
            String userId,
            String email,
            String role) {

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "userId",
                userId
        );

        claims.put(
                "role",
                role
        );

        long currentTime =
                System.currentTimeMillis();

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(
                        new Date(
                                currentTime
                        )
                )
                .expiration(
                        new Date(
                                currentTime
                                        + expirationMs
                        )
                )
                .signWith(
                        getKey()
                )
                .compact();
    }

    public String extractEmail(
            String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public String extractUserId(
            String token) {

        return extractClaim(
                token,
                claims ->
                        claims.get(
                                "userId",
                                String.class
                        )
        );
    }

    public String extractRole(
            String token) {

        return extractClaim(
                token,
                claims ->
                        claims.get(
                                "role",
                                String.class
                        )
        );
    }

    public boolean isTokenValid(
            String token) {

        return !isTokenExpired(
                token
        );
    }

    private boolean isTokenExpired(
            String token) {

        return extractExpiration(
                token
        ).before(
                new Date()
        );
    }

    private Date extractExpiration(
            String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> resolver) {

        Claims claims =
                extractAllClaims(
                        token
                );

        return resolver.apply(
                claims
        );
    }

    private Claims extractAllClaims(
            String token) {

        return Jwts.parser()
                .verifyWith(
                        getKey()
                )
                .build()
                .parseSignedClaims(
                        token
                )
                .getPayload();
    }

    private SecretKey getKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(
                        secretKey
                );

        return Keys.hmacShaKeyFor(
                keyBytes
        );
    }
}