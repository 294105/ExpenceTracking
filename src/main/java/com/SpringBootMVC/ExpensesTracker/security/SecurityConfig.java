package com.SpringBootMVC.ExpensesTracker.security;

import com.SpringBootMVC.ExpensesTracker.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * SecurityConfig configures Spring Security for the application.
 */
@Configuration
public class SecurityConfig {

    /**
     * Bean for BCryptPasswordEncoder, used to encode passwords.
     *
     * @return a BCryptPasswordEncoder instance.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the DaoAuthenticationProvider using the provided UserService
     * and the password encoder.
     *
     * @param userService the UserService implementation.
     * @return a configured DaoAuthenticationProvider instance.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    /**
     * Configures the SecurityFilterChain, establishing URL security,
     * login, and logout settings.
     *
     * @param http                              the HttpSecurity instance.
     * @param customAuthenticationSuccessHandler a custom authentication success handler.
     * @return a configured SecurityFilterChain.
     * @throws Exception if there is an error configuring HTTP security.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationSuccessHandler customAuthenticationSuccessHandler)
            throws Exception {

        http.authorizeHttpRequests(config -> config
                        // Permit access to static resources.
                        .requestMatchers("/css/**", "/assets/**", "/js/**").permitAll()
                        // Permit access to registration and landing pages.
                        .requestMatchers("/", "/showRegistrationForm", "/processRegistration").permitAll()
                        // All other requests require authentication.
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        // Set up a custom login page and processing URL.
                        .loginPage("/showLoginPage")
                        .loginProcessingUrl("/authenticateTheUser")
                        .successHandler(customAuthenticationSuccessHandler)
                        .permitAll())
                .logout(logout -> logout
                        // Configure logout with a custom URL and success URL.
                        .permitAll()
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/showLoginPage"));

        return http.build();
    }
}
