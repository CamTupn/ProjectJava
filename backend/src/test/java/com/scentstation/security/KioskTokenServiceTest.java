package com.scentstation.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class KioskTokenServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("scentstation-kiosk-test-secret-key-256-bits!!".getBytes(StandardCharsets.UTF_8));

    private KioskTokenService kioskTokenService;

    @BeforeEach
    void setUp() {
        kioskTokenService = new KioskTokenService(SECRET, 900_000L);
    }

    @Test
    void generateAndValidateToken_succeedsForMatchingOrder() {
        String token = kioskTokenService.generateToken(101L, 20L);

        assertTrue(kioskTokenService.isValidToken(token, 101L));
        assertEquals(101L, kioskTokenService.extractOrderId(token));
        assertEquals(20L, kioskTokenService.extractMachineId(token));
        kioskTokenService.validateTokenOrThrow(token, 101L);
    }

    @Test
    void isValidToken_returnsFalseForDifferentOrderOrTamperedToken() {
        String token = kioskTokenService.generateToken(101L, 20L);

        assertFalse(kioskTokenService.isValidToken(token, 999L));
        assertFalse(kioskTokenService.isValidToken(token + "tampered", 101L));
        assertFalse(kioskTokenService.isValidToken("", 101L));
        assertFalse(kioskTokenService.isValidToken(null, 101L));
        assertThrows(AccessDeniedException.class, () -> kioskTokenService.validateTokenOrThrow(token, 999L));
    }

    @Test
    void isValidToken_returnsFalseWhenExpired() {
        KioskTokenService expiredService = new KioskTokenService(SECRET, -1000L);
        String expiredToken = expiredService.generateToken(101L, 20L);

        assertFalse(kioskTokenService.isValidToken(expiredToken, 101L));
        assertThrows(AccessDeniedException.class, () -> kioskTokenService.validateTokenOrThrow(expiredToken, 101L));
    }
}
