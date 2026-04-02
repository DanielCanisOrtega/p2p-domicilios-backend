package com.p2pdomicilios.P2pDomicilios;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // <-- ESTO es lo que te está bloqueando el POST
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated() // Todo pide login
            )
            .httpBasic(withDefaults()); // Usar el admin/password123 de Postman
        
        return http.build();
    }
}