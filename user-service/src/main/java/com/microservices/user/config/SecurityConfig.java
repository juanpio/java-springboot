package com.microservices.user.config;

import com.microservices.user.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class for the user-service microservice.
 * <p>
 * This configuration implements JWT-based stateless authentication and authorization
 * for REST API endpoints. It configures Spring Security to validate JWT tokens on each
 * request without maintaining server-side sessions.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Stateless authentication using JWT tokens</li>
 *   <li>BCrypt password encoding for secure password storage</li>
 *   <li>Custom JWT authentication filter for token validation</li>
 *   <li>Method-level security with @PreAuthorize and @PostAuthorize support</li>
 *   <li>Public endpoints for authentication and monitoring</li>
 * </ul>
 * 
 * <h2>Security Flow:</h2>
 * <ol>
 *   <li>Client sends request with JWT token in Authorization header</li>
 *   <li>JwtAuthenticationFilter extracts and validates the token</li>
 *   <li>User details are loaded into Spring Security context</li>
 *   <li>Authorization rules are checked before processing request</li>
 * </ol>
 * 
 * @author User Service Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Provides a BCrypt password encoder bean for secure password hashing.
     * <p>
     * BCrypt is a strong, adaptive hashing algorithm that:
     * <ul>
     *   <li>Automatically generates salts to prevent rainbow table attacks</li>
     *   <li>Can be configured to become slower as hardware improves</li>
     *   <li>Makes brute-force attacks computationally expensive</li>
     * </ul>
     * </p>
     * 
     * @return a BCryptPasswordEncoder instance for hashing and verifying passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates a DAO-based authentication provider for validating user credentials.
     * <p>
     * This provider handles authentication by:
     * <ul>
     *   <li>Loading user details from the database via UserDetailsService</li>
     *   <li>Verifying the provided password against the stored BCrypt hash</li>
     *   <li>Returning an authenticated token if credentials are valid</li>
     * </ul>
     * </p>
     * 
     * @return a configured DaoAuthenticationProvider with UserDetailsService and PasswordEncoder
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager as a Spring bean.
     * <p>
     * The AuthenticationManager is the main entry point for authentication requests,
     * typically used by authentication controllers to validate user credentials during
     * login operations. It delegates to registered authentication providers (such as
     * the DaoAuthenticationProvider) to perform the actual authentication.
     * </p>
     * 
     * @param authConfig the Spring Security authentication configuration
     * @return the configured AuthenticationManager
     * @throws Exception if authentication manager cannot be retrieved
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configures the Spring Security filter chain with JWT-based stateless authentication.
     * <p>
     * This configuration includes:
     * <ul>
     *   <li><b>CSRF Protection:</b> Disabled for stateless JWT authentication (not vulnerable to CSRF)</li>
     *   <li><b>Session Management:</b> STATELESS - no HTTP sessions created or used</li>
     *   <li><b>Authorization Rules:</b>
     *     <ul>
     *       <li>/api/auth/** - Public access for login/registration endpoints</li>
     *       <li>/h2-console/** - Public access for H2 database console (development only)</li>
     *       <li>/actuator/** - Public access for Spring Boot Actuator endpoints</li>
     *       <li>All other endpoints - Require valid JWT authentication</li>
     *     </ul>
     *   </li>
     *   <li><b>JWT Filter:</b> Custom filter validates JWT tokens before standard authentication</li>
     *   <li><b>Frame Options:</b> Configured for H2 console iframe compatibility</li>
     * </ul>
     * </p>
     * 
     * <p><b>Security Warning:</b> In production environments, consider:
     * <ul>
     *   <li>Removing or securing H2 console access</li>
     *   <li>Restricting Actuator endpoints to authenticated admin users</li>
     *   <li>Adding CORS configuration if accessed by browser-based clients</li>
     * </ul>
     * </p>
     * 
     * @param http the HttpSecurity configuration object
     * @return the configured SecurityFilterChain
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/h2-console/**", "/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // For H2 Console
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
