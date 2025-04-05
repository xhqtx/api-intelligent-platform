import java.security.SecureRandom;
import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        // Generate a random key with at least 64 bytes (512 bits)
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[64];
        random.nextBytes(key);
        
        // Encode to Base64
        String encodedKey = Base64.getEncoder().encodeToString(key);
        
        System.out.println("New JWT secret key (Base64 encoded, 64 bytes when decoded):");
        System.out.println(encodedKey);
    }
}