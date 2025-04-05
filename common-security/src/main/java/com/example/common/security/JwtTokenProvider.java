package com.example.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final SecretKey secretKey;
    private final long validityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final String issuer;

    public JwtTokenProvider(String secretKeyString, long validityInMilliseconds, 
                          long refreshTokenValidityInMilliseconds, String issuer) {
        this.secretKey = createSecretKey(secretKeyString);
        this.validityInMilliseconds = validityInMilliseconds;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
        this.issuer = issuer;
        logger.info("JwtTokenProvider initialized with issuer: {}", issuer);
    }

    private SecretKey createSecretKey(String secretKeyString) {
        if (secretKeyString == null || secretKeyString.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret key cannot be null or empty");
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
            logger.debug("Creating JWT secret key with length: {} bytes", keyBytes.length);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid JWT secret key format. Key must be Base64 encoded.", e);
            throw new IllegalArgumentException("Invalid JWT secret key format", e);
        }
    }

    public String createToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("auth", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))  // Remove "ROLE_" prefix
                .collect(Collectors.toList()));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(String username) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        
        // 获取 auth 字段中的权限列表
        List<?> authList = claims.get("auth", List.class);
        
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (authList != null) {
            authorities = authList.stream()
                    .map(Object::toString)
                    .map(auth -> auth.replace("[", "").replace("]", ""))  // Remove square brackets if present
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        
        logger.debug("Extracted authorities from token: {}", authorities);
        
        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> getAuthorities(String token) {
        Claims claims = getClaims(token);
        return claims.get("auth", List.class);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            // 验证令牌是否过期
            if (claims.getBody().getExpiration().before(new Date())) {
                logger.warn("Expired JWT token");
                return false;
            }

            // 验证令牌签发者
            String tokenIssuer = claims.getBody().getIssuer();
            if (!issuer.equals(tokenIssuer)) {
                logger.warn("Invalid JWT issuer: {}", tokenIssuer);
                return false;
            }

            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getValidityInMilliseconds() {
        return validityInMilliseconds;
    }

    public long getRefreshTokenValidityInMilliseconds() {
        return refreshTokenValidityInMilliseconds;
    }

    public long getAccessTokenExpirationTime() {
        return validityInMilliseconds;
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            // 刷新令牌不包含auth字段
            return !claims.containsKey("auth");
        } catch (Exception e) {
            logger.error("Error checking refresh token: {}", e.getMessage());
            return false;
        }
    }
}