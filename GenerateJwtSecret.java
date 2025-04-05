import java.security.SecureRandom;
import java.util.Base64;

public class GenerateJwtSecret {
    public static void main(String[] args) {
        // Create a random key with at least 64 bytes (512 bits)
        byte[] keyBytes = new byte[64]; // 64 bytes = 512 bits
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(keyBytes);
        
        // Convert the key to a Base64 encoded string
        String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
        
        System.out.println("Generated new JWT secret key:");
        System.out.println(encodedKey);
    }
}