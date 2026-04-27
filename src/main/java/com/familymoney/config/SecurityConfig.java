package com.familymoney.config;

import com.familymoney.security.JwtAuthFilter;
import com.familymoney.security.JwtAuthenticationEntryPoint;
import com.familymoney.types.Role;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.DispatcherTypeRequestMatcher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private static final String API_BASE_PATH = "/api";

  private final JwtAuthFilter jwtAuthFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {

    // Add "/api" prefix to all API endpoints
    PathPatternRequestMatcher.Builder api =
        PathPatternRequestMatcher.withDefaults().basePath(API_BASE_PATH);

    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .exceptionHandling(ex -> ex.authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers(new DispatcherTypeRequestMatcher(DispatcherType.FORWARD))
                    .permitAll()
                    .requestMatchers(new DispatcherTypeRequestMatcher(DispatcherType.ERROR))
                    .permitAll()

                    // Root endpoints (NOT under /api)
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()

                    // Versioned API endpoints
                    .requestMatchers(api.matcher("/v*/auth/**"))
                    .permitAll()
                    .requestMatchers(
                        api.matcher("/v*/users/**"),
                        api.matcher("/v*/groups/**"),
                        api.matcher("/v*/transactions/**"))
                    .authenticated()
                    .requestMatchers(api.matcher("/v*/admin/**"))
                    .hasRole(Role.ADMIN.toString())

                    // Any other request should be denied
                    .anyRequest()
                    .denyAll())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
