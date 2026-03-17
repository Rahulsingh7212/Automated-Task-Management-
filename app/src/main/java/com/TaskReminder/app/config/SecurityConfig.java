package com.TaskReminder.app.config;

import com.TaskReminder.app.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for API endpoints, enable for web forms
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )

                // URL Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        // Public pages - accessible without login
                        .requestMatchers(
                                "/auth/**",
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/static/**"
                        ).permitAll()

                        // API endpoints
                        .requestMatchers("/api/**").permitAll()

                        // All other pages require login
                        .anyRequest().authenticated()
                )

                // Login Configuration
                .formLogin(form -> form
                        .loginPage("/auth/login")           // Custom login page
                        .loginProcessingUrl("/auth/login")   // Form action URL
                        .usernameParameter("email")          // Use email as username
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true) // After login go to dashboard
                        .failureUrl("/auth/login?error=true")  // On failure
                        .permitAll()
                )

                // Logout Configuration
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // ✅ Allow GET request for logout
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutRequestMatcher(
                                new AntPathRequestMatcher("/auth/logout", "GET")  // ← Allow GET
                        )
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )

                // Remember Me
                .rememberMe(remember -> remember
                        .key("uniqueAndSecretKey")
                        .tokenValiditySeconds(86400) // 1 day
                        .userDetailsService(userDetailsService)
                );

        return http.build();
    }
}