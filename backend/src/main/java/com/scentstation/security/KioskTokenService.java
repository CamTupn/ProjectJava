package com.scentstation.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class KioskTokenService {

    private static final String TOKEN_TYPE = "KIOSK_ORDER";

    private final SecretKey secretKey;
    private final long expirationMs;

    public KioskTokenService(
            @Value("${app.kiosk.token.secret:${app.jwt.secret}}") String secret,
            @Value("${app.kiosk.token.expiration:900000}") long expirationMs
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long orderId, Long machineId) {
        if (orderId == null || machineId == null) {
            throw new IllegalArgumentException("orderId and machineId are required to generate kiosk token");
        }
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(orderId))
                .claim("orderId", orderId)
                .claim("machineId", machineId)
                .claim("type", TOKEN_TYPE)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public boolean isValidToken(String token, Long expectedOrderId) {
        if (token == null || token.isBlank() || expectedOrderId == null) {
            return false;
        }
        try {
            Claims claims = parseClaims(token);
            String type = claims.get("type", String.class);
            Long tokenOrderId = toLong(claims.get("orderId"));
            Long tokenMachineId = toLong(claims.get("machineId"));
            return TOKEN_TYPE.equals(type)
                    && expectedOrderId.equals(tokenOrderId)
                    && tokenMachineId != null;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public void validateTokenOrThrow(String token, Long expectedOrderId) {
        if (!isValidToken(token, expectedOrderId)) {
            throw new AccessDeniedException("Invalid or expired kiosk access token");
        }
    }

    public Long extractOrderId(String token) {
        Claims claims = parseClaims(token);
        return toLong(claims.get("orderId"));
    }

    public Long extractMachineId(String token) {
        Claims claims = parseClaims(token);
        return toLong(claims.get("machineId"));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
