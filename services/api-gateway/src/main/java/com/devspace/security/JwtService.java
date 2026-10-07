package com.devspace.gateway.security;

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

    public String extractEmail(
            String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public boolean isTokenValid(
            String token) {

        try {

            extractAllClaims(
                    token
            );

            return true;

        } catch (Exception ex) {

            return false;
        }
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