package com.scentstation.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration) {

        byte[] keyBytes = java.util.Base64.getDecoder()
                .decode(secret);

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    // Generate JWT
    public String generateToken(String username, Map<String, Object> claims) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // Extract username
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract user ID
    public Long extractUserId(String token) {

        Object userId = extractClaim(
                token,
                claims -> claims.get("userId")
        );

        if (userId instanceof Number number) {
            return number.longValue();
        }

        return null;
    }

    // Extract tenant ID
    public Long extractTenantId(String token) {

        Object tenantId = extractClaim(
                token,
                claims -> claims.get("tenantId")
        );

        if (tenantId instanceof Number number) {
            return number.longValue();
        }

        return null;
    }

    // Extract tenant code
    public String extractTenantCode(String token) {
        return extractClaim(
                token,
                claims -> claims.get("tenantCode", String.class)
        );
    }

    // Extract full name
    public String extractFullName(String token) {
        return extractClaim(
                token,
                claims -> claims.get("fullName", String.class)
        );
    }

    // Extract roles from JWT
    public List<String> extractRoles(String token) {

        Object roles = extractClaim(
                token,
                claims -> claims.get("roles")
        );

        if (!(roles instanceof Collection<?> roleCollection)) {
            return List.of();
        }

        return roleCollection.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toList());
    }

    // Extract a specific claim
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Check token expiration
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate =
                    extractClaim(token, Claims::getExpiration);

            return expirationDate.before(new Date());

        } catch (ExpiredJwtException exception) {
            return true;
        }
    }

    // Validate token
    public boolean isTokenValid(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);

            return extractedUsername.equals(username)
                    && !isTokenExpired(token);

        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}
