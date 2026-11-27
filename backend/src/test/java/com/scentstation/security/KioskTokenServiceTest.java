package com.scentstation.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KioskTokenServiceTest {
    private final KioskTokenService service = new KioskTokenService("test-secret");

    @Test
    void acceptsTokenIssuedForSameOrder() {
        String token = service.issue(10L);

        assertTrue(service.verify(token, 10L));
    }

    @Test
    void rejectsTokenForAnotherOrderOrTamperedToken() {
        String token = service.issue(10L);

        assertFalse(service.verify(token, 11L));
        assertFalse(service.verify(token + "x", 10L));
    }
}
