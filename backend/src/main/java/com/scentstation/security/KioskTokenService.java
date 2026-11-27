package com.scentstation.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * TASK-025 owns the full kiosk-session token lifecycle. Payment only depends
 * on this narrow verification seam so the provider cannot trust order ids from
 * an unbound client request.
 */
@Service
public class KioskTokenService {
    private final String secret;

    public KioskTokenService(@Value("${app.kiosk.token-secret:${JWT_SECRET:}}") String secret) {
        this.secret = secret == null ? "" : secret;
    }

    public boolean verify(String token, Long orderId) {
        if (secret.isBlank() || token == null || orderId == null) return false;
        String[] parts = token.split("\\.", -1);
        if (parts.length != 2 || !parts[0].equals(orderId.toString())) return false;
        try {
            byte[] expected = sign(parts[0]);
            byte[] actual = Base64.getUrlDecoder().decode(parts[1]);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public String issue(Long orderId) {
        if (secret.isBlank() || orderId == null) {
            throw new IllegalStateException("Kiosk token secret is not configured");
        }
        return orderId + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(sign(orderId.toString()));
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign kiosk token", ex);
        }
    }
}
