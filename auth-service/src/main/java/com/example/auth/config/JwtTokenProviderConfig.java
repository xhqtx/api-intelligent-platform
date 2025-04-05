package com.example.auth.config;

import com.example.common.security.JwtConfig;
import com.example.common.security.JwtTokenProvider;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Base64;

@Configuration
public class JwtTokenProviderConfig {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProviderConfig.class);
    private final JwtConfig jwtConfig;

    public JwtTokenProviderConfig(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Bean
    public SecretKey jwtSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtConfig.getSecret());
        if (keyBytes.length < 64) { // 确保密钥至少有512位
            return Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
        } else {
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        logger.info("Initializing JwtTokenProvider with issuer: {}", jwtConfig.getIssuer());
        // 直接使用配置的密钥字符串，而不是重新编码
        return new JwtTokenProvider(
                jwtConfig.getSecret(),
                jwtConfig.getExpiration(),
                jwtConfig.getRefreshTokenExpiration(),
                jwtConfig.getIssuer()
        );
    }
}