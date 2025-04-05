package com.example.common.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class JwtKeyGenerator {

    public static SecretKey generateKey(String secret) {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        if (decodedKey.length < 64) {
            throw new IllegalArgumentException("JWT secret key must be at least 64 bytes long after Base64 decoding");
        }
        return Keys.hmacShaKeyFor(decodedKey);
    }

    public static String generateNewKeyString() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[64]; // 512 bits
        random.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }
}