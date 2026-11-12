
package com.scentstation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            Base64.getEncoder().encodeToString(new byte[32]);

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60000);
    }

    // Test 1: Generate token successfully
    @Test
    void shouldGenerateTokenSuccessfully() {
        String token = jwtService.generateToken(
                "brandadmin",
                new HashMap<>()
        );

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("brandadmin",
                jwtService.extractUsername(token));
    }

    // Test 2: Extract custom claims
    @Test
    void shouldExtractCustomClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("role", "BRAND_ADMIN");

        String token = jwtService.generateToken(
                "brandadmin",
                claims
        );

        String role = jwtService.extractClaim(
                token,
                allClaims -> allClaims.get("role", String.class)
        );

        assertEquals("BRAND_ADMIN", role);
    }

    // Test 3: Validate correct token
    @Test
    void shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken(
                "brandadmin",
                new HashMap<>()
        );

        assertTrue(jwtService.isTokenValid(
                token, "brandadmin"));
    }

    // Test 4: Reject incorrect username
    @Test
    void shouldReturnFalseForWrongUsername() {
        String token = jwtService.generateToken(
                "brandadmin",
                new HashMap<>()
        );

        assertFalse(jwtService.isTokenValid(
                token, "anotheruser"));
    }

    // Test 5: Reject tampered token
    @Test
    void shouldReturnFalseForTamperedToken() {
        String token = jwtService.generateToken(
                "brandadmin",
                new HashMap<>()
        );

        String tamperedToken = token + "invalid";

        assertFalse(jwtService.isTokenValid(
                tamperedToken, "brandadmin"));
    }

    // Test 6: Detect expired token
    @Test
    void shouldDetectExpiredToken() throws InterruptedException {
        JwtService shortLivedJwtService =
                new JwtService(SECRET, 1500);

        String token = shortLivedJwtService.generateToken(
                "brandadmin",
                new HashMap<>()
        );

        Thread.sleep(2500);

        assertTrue(shortLivedJwtService.isTokenExpired(token));

        assertFalse(shortLivedJwtService.isTokenValid(
                token, "brandadmin"));
    }
}
