package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.auth.security.JwtTokenFilter;
import com.example.common.security.JwtTokenProvider;
import com.example.auth.service.RedisService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.core.GrantedAuthorityDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
public class SecurityConfig {

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // Remove the ROLE_ prefix
        return new GrantedAuthorityDefaults("");
    }

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RedisService redisService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService, RedisService redisService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.redisService = redisService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().disable()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/users/**").hasAnyAuthority("user:view", "user:create", "user:update", "user:delete", "user:reset-pwd", "user:manage", "admin")
            .requestMatchers("/api/roles/**").hasAnyAuthority("role:view", "role:add", "role:edit", "role:delete", "role:grant", "admin")
            .requestMatchers("/api/permissions/**").hasAnyAuthority("permission:read", "permission:create", "permission:update", "permission:delete", "permission:manage", "admin")
            .requestMatchers("/api/permission-groups/**").hasAnyAuthority("permission:group:manage", "admin")
            .requestMatchers("/api/dict/**").hasAnyAuthority("dict:view", "dict:create", "dict:update", "dict:delete", "admin")
            .requestMatchers("/api/logs/**").hasAnyAuthority("log:view", "log:export", "log:delete", "admin")
            .requestMatchers("/api/system/**").hasAnyAuthority("system:view", "system:config", "system:monitor", "admin")
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(new JwtTokenFilter(jwtTokenProvider, userDetailsService, redisService), 
                           UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

}