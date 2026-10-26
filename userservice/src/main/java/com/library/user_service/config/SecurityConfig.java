package com.library.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/neighborhoods",
                                "/api/auth/google/**",
                                "/api/onboarding/**",
                                "/login/**",
                                "/oauth2/**"
                        ).permitAll()
                        // קריאות פנימיות בלבד (שרת-לשרת) מ-book-service/notification-service/ai-service.
                        // אין לחשוף את הפורט הזה (9000) מחוץ לרשת הדוקר הפנימית.
                        // TODO: אם רוצים הגנה טובה יותר - להוסיף בדיקת API key פנימי במקום permitAll גורף.
                        .requestMatchers(HttpMethod.POST, "/api/activities").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/internal/**").permitAll()
                        .anyRequest().authenticated()
                )
                // 🟢 מוסיפים true בסוף כדי לאלץ את ספרינג להפנות ישירות ל-success שלכן
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/api/auth/google/success", true))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("http://localhost:3000")); // מאשר לריאקט לגשת
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true); // מאפשר העברת עוגיות/טוקנים במידת הצורך

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
