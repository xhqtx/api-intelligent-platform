package com.example.gateway.filter;

import com.example.common.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final String headerString;
    private final String tokenPrefix;

    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, 
                                  @Value("${jwt.headerString:Authorization}") String headerString,
                                  @Value("${jwt.tokenPrefix:Bearer }") String tokenPrefix) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.headerString = headerString;
        this.tokenPrefix = tokenPrefix;
        logger.info("JwtAuthenticationFilter initialized with header: {}, prefix: {}", headerString, tokenPrefix);
    }

    private static final Set<String> AUTH_WHITELIST = new HashSet<>(List.of(
            "/auth/api/auth/login",
            "/auth/api/auth/register",
            "/auth/api/auth/refresh",
            "/auth/api/auth/logout",
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/actuator/**"
    ));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        // 检查是否是白名单路径
        if (isWhitelistedPath(path)) {
            logger.debug("Path {} is whitelisted, skipping JWT validation", path);
            return chain.filter(exchange);
        }

        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst(headerString);
        
        // 如果没有Authorization头或格式不正确
        if (authHeader == null || !authHeader.startsWith(tokenPrefix)) {
            logger.warn("Missing or invalid Authorization header for path: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        // 提取token
        String token = authHeader.substring(tokenPrefix.length());
        
        // 验证token
        boolean isValid = jwtTokenProvider.validateToken(token);
        logger.debug("JWT token validation result: {}", isValid);
        if (!isValid) {
            logger.warn("Invalid JWT token for path: {}", path);
            return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }

        // 如果token有效，添加用户信息到请求头
        try {
            String username = jwtTokenProvider.getUsername(token);
List<String> authorities = jwtTokenProvider.getAuthorities(token);
// 确保权限格式正确，移除可能的方括号
List<String> cleanAuthorities = authorities.stream()
        .map(auth -> auth.replace("[", "").replace("]", ""))
        .collect(Collectors.toList());
        
ServerHttpRequest modifiedRequest = request.mutate()
        .header("X-User", username)
        .header("X-Authorities", String.join(",", cleanAuthorities))
        .build();

logger.debug("JWT token validated for user: {} with authorities: {} accessing: {}", username, authorities, path);
return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            logger.error("Error processing JWT token", e);
            return onError(exchange, "Error processing JWT token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isWhitelistedPath(String path) {
        return AUTH_WHITELIST.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String errorJson = String.format("{\"error\":\"%s\"}", message);
        byte[] bytes = errorJson.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级
    }
}