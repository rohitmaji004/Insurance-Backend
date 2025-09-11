package com.icdc.insurance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Disable CSRF for local dev
                .authorizeHttpRequests()
                .anyRequest().permitAll() // Allow all requests
                .and()
                .httpBasic().disable(); // Disable basic auth

        return http.build();
    }
}
