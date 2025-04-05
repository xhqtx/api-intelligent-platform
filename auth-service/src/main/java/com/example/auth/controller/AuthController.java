package com.example.auth.controller;

import com.example.auth.controller.dto.LoginRequest;
import com.example.auth.controller.dto.TokenRefreshRequest;
import com.example.auth.controller.dto.TokenRefreshResponse;
import com.example.auth.controller.dto.UserRegistrationRequest;
import com.example.auth.model.User;
import com.example.auth.service.RedisService;
import com.example.common.security.ApiResponse;
import com.example.common.security.JwtTokenProvider;
import com.example.auth.service.UserService;
import com.example.auth.utils.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtTokenProvider jwtTokenProvider, RedisService redisService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisService = redisService;
    }

    // 将认证逻辑抽取为私有方法
    private Authentication authenticateUser(String username, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }

    // 将生成令牌逻辑抽取为私有方法
    private TokenRefreshResponse generateTokenResponse(Authentication authentication) {
        String accessToken = jwtTokenProvider.createToken(authentication.getName(), authentication.getAuthorities());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication.getName());
        String username = authentication.getName();
        redisService.saveRefreshToken(username, refreshToken, jwtTokenProvider.getRefreshTokenValidityInMilliseconds());
        return new TokenRefreshResponse(accessToken, refreshToken);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("User {} successfully logged in", loginRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Login successful", generateTokenResponse(authentication)));
        } catch (AuthenticationException e) {
            logger.warn("Failed login attempt for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid username or password"));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        logger.error("An error occurred: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody UserRegistrationRequest registerRequest) {
        User user = userService.registerUser(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getEmail());
        return ResponseEntity.ok(ApiResponse.success("User registered successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtTokenProvider.getUsername(token);

            // 将访问令牌加入黑名单
            redisService.addToBlacklist(token, jwtTokenProvider.getAccessTokenExpirationTime());

            // 删除刷新令牌
            redisService.deleteRefreshToken(username);

            return ResponseEntity.ok(ApiResponse.success("Successfully logged out"));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid token format"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser() {
        // 从SecurityContext获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByUsername(authentication.getName());

            if (user != null) {
                // 构建用户信息响应
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("enabled", user.isEnabled());
                // 获取用户角色列表，包含角色ID和名称
                // 添加角色信息
                userInfo.put("roles", user.getRoles().stream()
                    .map(role -> {
                        Map<String, Object> roleInfo = new HashMap<>();
                        roleInfo.put("id", role.getId());
                        roleInfo.put("name", role.getName());
                        // 添加该角色的所有权限
                        roleInfo.put("permissions", role.getPermissions().stream()
                            .map(permission -> {
                                Map<String, Object> permissionInfo = new HashMap<>();
                                permissionInfo.put("id", permission.getId());
                                permissionInfo.put("name", permission.getName());
                                permissionInfo.put("description", permission.getDescription());
                                return permissionInfo;
                            })
                            .collect(Collectors.toList()));
                        return roleInfo;
                    })
                    .collect(Collectors.toList()));
                
                // 添加用户的所有权限（合并所有角色的权限）
                userInfo.put("authorities", user.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toList()));

                return ResponseEntity.ok(ApiResponse.success(userInfo));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("User not authenticated"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        if (jwtTokenProvider.validateToken(requestRefreshToken) && jwtTokenProvider.isRefreshToken(requestRefreshToken)) {
            String username = jwtTokenProvider.getUsername(requestRefreshToken);
            String storedRefreshToken = redisService.getRefreshToken(username);

            if (storedRefreshToken != null && storedRefreshToken.equals(requestRefreshToken)) {
                User user = userService.findByUsername(username);
                if (user != null) {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    String newAccessToken = jwtTokenProvider.createToken(authentication.getName(), authentication.getAuthorities());
                    return ResponseEntity.ok(ApiResponse.success(new TokenRefreshResponse(newAccessToken, requestRefreshToken)));
                }
            }
        }

        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid refresh token"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        
        Pageable pageable = PageUtils.convertToPageable(page, size, sortBy, sortDirection);
        // 已经使用了 PageUtils.convertToPageable，无需修改
        Page<User> users = userService.findAllUsers(pageable);

        return ResponseEntity.ok(ApiResponse.success(users));
    }
}