import java.security.SecureRandom;
import java.util.Base64;

public class JwtSecretGenerator {
    private static final int KEY_SIZE_BYTES = 64; // 512 bits

    public static void main(String[] args) {
        try {
            // 创建一个安全的随机数生成器
            SecureRandom secureRandom = new SecureRandom();

            // 生成随机字节
            byte[] key = new byte[KEY_SIZE_BYTES];
            secureRandom.nextBytes(key);

            // 将字节转换为Base64编码的字符串
            String base64Key = Base64.getEncoder().encodeToString(key);

            System.out.println("Generated JWT Secret Key (Base64 encoded, " + KEY_SIZE_BYTES + " bytes):");
            System.out.println(base64Key);
            System.out.println("\nKey length: " + key.length + " bytes");
            System.out.println("Base64 string length: " + base64Key.length() + " characters");

            // 验证解码后的长度
            byte[] decoded = Base64.getDecoder().decode(base64Key);
            System.out.println("Decoded length: " + decoded.length + " bytes");

        } catch (Exception e) {
            System.err.println("Error generating JWT secret key: " + e.getMessage());
            e.printStackTrace();
        }
    }
}