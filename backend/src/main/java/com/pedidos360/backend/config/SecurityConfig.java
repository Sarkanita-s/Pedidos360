package com.pedidos360.backend.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private static final String ISSUER = "https://sts.windows.net/501361f2-a641-4fba-b6a9-954644204a59/";

        private static final String AUDIENCE = "api://ac3261d7-bac7-48e8-b189-1d5b9c93c107";

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())

                                .cors(cors -> cors.configurationSource(
                                                corsConfigurationSource()))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                org.springframework.http.HttpMethod.OPTIONS,
                                                                "/**")
                                                .permitAll()
                                                .requestMatchers("/error").permitAll()
                                                .anyRequest().authenticated())

                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                jwtAuthenticationConverter())));

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

                                        if (audience instanceof String audienceString) {
                                                return AUDIENCE.equals(audienceString);
                                        }

                                        if (audience instanceof java.util.Collection<?> audiences) {
                                                return audiences.contains(AUDIENCE);
                                        }

                                        return false;
                                });

                decoder.setJwtValidator(
                                JwtValidators.createDefaultWithValidators(
                                                List.of(
                                                                issuerValidator,
                                                                audienceValidator)));

                return decoder;
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(
                                Collections.singletonList(
                                                "http://localhost:4200"));

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
                                                "Content-Type",
                                                "Accept",
                                                "Origin"));

                configuration.setExposedHeaders(
                                Collections.singletonList(
                                                "Authorization"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration(
                                "/**",
                                configuration);

                return source;
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {

                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

                converter.setJwtGrantedAuthoritiesConverter(jwt -> {

                        Object roles = jwt.getClaims().get("roles");

                        if (!(roles instanceof java.util.Collection<?> roleCollection)) {
                                return Collections.emptyList();
                        }

                        return roleCollection.stream()
                                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                        "ROLE_" + role.toString()))
                                        .map(authority -> (org.springframework.security.core.GrantedAuthority) authority)
                                        .toList();
                });

                return converter;
        }

}