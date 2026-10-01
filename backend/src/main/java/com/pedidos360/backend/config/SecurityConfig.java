package com.pedidos360.backend.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private static final String ISSUER = "https://sts.windows.net/501361f2-a641-4fba-b6a9-954644204a59/";

        private static final String AUDIENCE = "api://ac3261d7-bac7-48e8-b189-1d5b9c93c107";

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())

                                .cors(cors -> {
                                })

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/error").permitAll()
                                                .anyRequest().authenticated())

                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                                }));

                return http.build();
        }

        @Bean
        public JwtDecoder jwtDecoder() {

                NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(
                                "https://login.microsoftonline.com/501361f2-a641-4fba-b6a9-954644204a59/discovery/keys")
                                .build();

                OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(ISSUER);

                OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<>(
                                "aud",
                                audience -> {
                                        if (!(audience instanceof java.util.List<?> audiences)) {
                                                return false;
                                        }

                                        return audiences.contains(AUDIENCE);
                                });

                decoder.setJwtValidator(
                                JwtValidators.createDefaultWithValidators(
                                                issuerValidator,
                                                audienceValidator));

                return decoder;
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(
                                Collections.singletonList("http://localhost:4200"));

                configuration.setAllowedMethods(
                                Arrays.asList(
                                                "GET",
                                                "POST",
                                                "PUT",
                                                "DELETE",
                                                "OPTIONS"));

                configuration.setAllowedHeaders(
                                Arrays.asList(
                                                "Authorization",
                                                "Content-Type"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}