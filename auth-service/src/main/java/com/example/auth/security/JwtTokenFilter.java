package com.example.auth.security;

import com.example.common.security.JwtConfig;
import com.example.common.security.JwtTokenProvider;
import com.example.auth.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RedisService redisService;
    private JwtConfig jwtConfig;
    private String headerString = "Authorization";
    private String tokenPrefix = "Bearer ";


    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService, RedisService redisService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.redisService = redisService;
    }

    public String getHeaderString() {
        return jwtConfig != null ? jwtConfig.getHeaderString() : headerString;
    }

    public String getTokenPrefix() {
        return jwtConfig != null ? jwtConfig.getTokenPrefix() : tokenPrefix;
    }

    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    // 保留原有构造函数以兼容可能的其他调用
    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider, JwtConfig jwtConfig) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtConfig = jwtConfig;
        this.userDetailsService = null;
        this.redisService = null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        logger.debug("Resolved token: {}", token);

        try {
            if (token != null) {
                logger.debug("Validating token...");
                if (jwtTokenProvider.validateToken(token)) {
                    logger.debug("Token is valid. Getting authentication...");
                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    logger.debug("Authentication: {}", auth);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.debug("Authentication set in SecurityContextHolder");
                } else {
                    logger.warn("Invalid token");
                }
            } else {
                logger.debug("No token found in the request");
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token", e);
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(getHeaderString());
        if (bearerToken != null && bearerToken.startsWith(getTokenPrefix())) {
            return bearerToken.substring(getTokenPrefix().length()).trim();
        }
        return null;
    }
}