package com.example.common.security;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * 用于生成和验证JWT密钥的工具类
 */
public class JwtSecretGenerator {
    
    public static void main(String[] args) {
        generateAndValidateNewKey();
    }
    
    public static void generateAndValidateNewKey() {
        // 生成新的密钥
        String newSecret = JwtKeyGenerator.generateNewKeyString();
        System.out.println("New JWT Secret Key (Base64 encoded):");
        System.out.println(newSecret);
        
        // 验证新生成的密钥
        try {
            SecretKey key = JwtKeyGenerator.generateKey(newSecret);
            System.out.println("\nKey validation successful! The key meets all requirements.");
            System.out.println("Key algorithm: " + key.getAlgorithm());
            System.out.println("Original key length: " + key.getEncoded().length + " bytes");
            
            // 打印Base64编码字符串长度
            System.out.println("Base64 encoded length: " + newSecret.length() + " characters");
            
            // 打印解码后的密钥长度
            byte[] decodedKey = Base64.getDecoder().decode(newSecret);
            System.out.println("Decoded key length: " + decodedKey.length + " bytes");
            
            // 检查填充字符
            int paddingCount = 0;
            for (int i = newSecret.length() - 1; i >= 0 && newSecret.charAt(i) == '='; i--) {
                paddingCount++;
            }
            System.out.println("Number of padding characters: " + paddingCount);
            
            // 计算理论原始字节数
            int originalByteCount = (int) Math.ceil(newSecret.length() * 3 / 4.0);
            System.out.println("Theoretical original byte count: " + originalByteCount);
            
            // 提供使用说明
            System.out.println("\nUsage Instructions:");
            System.out.println("1. Add this key to your application.yml or application.properties file:");
            System.out.println("   jwt.secret: " + newSecret);
            System.out.println("2. Ensure this key is kept secure and not exposed in public repositories.");
            System.out.println("3. For production environments, consider using environment variables or a secrets manager.");
        } catch (Exception e) {
            System.err.println("\nKey validation failed: " + e.getMessage());
        }
    }
}