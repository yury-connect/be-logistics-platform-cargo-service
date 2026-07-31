package com.innowise.logistics.cargoservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Включает @PreAuthorize, @Secured и т.д.
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ======== ОТКЛЮЧАЕМ CSRF (для REST API) ========
                .csrf(csrf -> csrf.disable())

                // ======== НАСТРОЙКА CORS ========
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ======== АВТОРИЗАЦИЯ ЗАПРОСОВ ========
                .authorizeHttpRequests(authz -> authz
                        // 🟢 Публичные эндпоинты (не требуют токена)
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ✅ ВАЖНО: Разрешаем OPTIONS-запросы для CORS preflight
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // 🔴 ВСЕ ОСТАЛЬНЫЕ запросы требуют аутентификации
                        .anyRequest().authenticated()
                )

                // ======== НАСТРОЙКА JWT ========
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )

                // ======== СТАТЕЛЕСС (без сессий) ========
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    /**
     * 🟢 НАСТРОЙКА CORS
     * Заменяет отдельный CorsConfig.java
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Разрешаем все источники (для разработки)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // ✅ Разрешаем все методы
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // ✅ Разрешаем все заголовки
        configuration.setAllowedHeaders(List.of("*"));

        // ✅ Разрешаем credentials (если нужно)
        configuration.setAllowCredentials(true);

        // ✅ Кэшируем preflight запросы на 1 час
        configuration.setMaxAge(3600L);

        // ✅ Добавляем заголовки, которые возвращаются в ответе
        configuration.setExposedHeaders(List.of(
                "Authorization", "Content-Type", "X-User-Id", "X-User-Roles"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 🟢 КОНВЕРТЕР JWT → РОЛИ SPRING SECURITY
     * Извлекает роли из поля "logistics-roles" в токене
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Извлекаем роли из поля "logistics-roles"
            List<String> roles = jwt.getClaim("logistics-roles");
            if (roles != null) {
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }

            return authorities;
        });
        return converter;
    }
}
