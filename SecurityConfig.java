package com.example.demo.config;

import com.example.demo.service.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AppUserDetailsService appUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF, common for APIs
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/tire-requests/images/**").permitAll() // Public access to images
                    
                    // Employee API security
                    .requestMatchers(HttpMethod.POST, "/api/employees/").hasRole("MANAGER") // Only managers can create employees
                    .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasRole("MANAGER")
                    .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyRole("MANAGER", "USER", "TTO") // All authenticated can view some employee data
                    .requestMatchers(HttpMethod.GET, "/api/employees/").hasRole("MANAGER") // Only manager can view all employees

                    // Tire Request API security
                    .requestMatchers(HttpMethod.POST, "/api/tire-requests/").hasAnyRole("USER", "MANAGER", "TTO") // Any authenticated user can create a request
                    .requestMatchers(HttpMethod.PUT, "/api/tire-requests/{id}/status").hasAnyRole("MANAGER", "TTO") // Only manager/TTO can update status
                    .requestMatchers(HttpMethod.GET, "/api/tire-requests/").hasAnyRole("MANAGER", "TTO") // Manager/TTO can see all requests
                    .requestMatchers(HttpMethod.GET, "/api/tire-requests/{id}").hasAnyRole("USER", "MANAGER", "TTO")
                    .requestMatchers(HttpMethod.GET, "/api/tire-requests/user/**").hasAnyRole("USER", "MANAGER", "TTO")
                    .requestMatchers(HttpMethod.GET, "/api/tire-requests/status/**").hasAnyRole("MANAGER", "TTO")
                    .requestMatchers(HttpMethod.DELETE, "/api/tire-requests/{tireRequestId}/images/**").hasAnyRole("USER", "MANAGER", "TTO") // User who owns, or Manager/TTO
                    
                    .anyRequest().authenticated() // All other requests require authentication
            )
            .formLogin(withDefaults()) // Enable form-based login for session authentication
            .logout(logout -> logout.permitAll()); // Enable logout

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(appUserDetailsService)
                                    .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}
