package com.example.common.security;

import javax.crypto.SecretKey;
import java.util.Base64;

public class GenerateJwtSecret {
    public static void main(String[] args) {
        String newSecret = JwtKeyGenerator.generateNewKeyString();
        System.out.println("New JWT Secret Key (Base64 encoded):");
        System.out.println(newSecret);
        
        // Validate the newly generated key
        try {
            SecretKey key = JwtKeyGenerator.generateKey(newSecret);
            System.out.println("\nKey validation successful! The key meets all requirements.");
            System.out.println("Original key length: " + key.getEncoded().length + " bytes");
            
            // Print Base64 encoded string length
            System.out.println("Base64 encoded length: " + newSecret.length() + " characters");
            
            // Print decoded key length
            byte[] decodedKey = Base64.getDecoder().decode(newSecret);
            System.out.println("Decoded key length: " + decodedKey.length + " bytes");
            
            // Check for padding characters
            int paddingCount = 0;
            for (int i = newSecret.length() - 1; i >= 0 && newSecret.charAt(i) == '='; i--) {
                paddingCount++;
            }
            System.out.println("Number of padding characters: " + paddingCount);
            
            // Calculate theoretical original byte count
            int originalByteCount = (int) Math.ceil(newSecret.length() * 3 / 4.0);
            System.out.println("Theoretical original byte count: " + originalByteCount);
        } catch (Exception e) {
            System.err.println("\nKey validation failed: " + e.getMessage());
        }
    }
}