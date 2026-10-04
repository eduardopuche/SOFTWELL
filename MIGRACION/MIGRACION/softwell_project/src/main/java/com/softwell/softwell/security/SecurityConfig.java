package com.softwell.softwell.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Habilita @PreAuthorize en controllers
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas — accesibles sin login
                .requestMatchers(
                    "/", "/catalogo", "/productos/**",
                    "/login", "/registro",
                    "/*.css", "/*.js", "/*.ico",
                    "/img/**", "/webjars/**", "/uploads/**",
                    "/*.webp", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif", "/*.svg"
                ).permitAll()

                // Solo ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Solo PROVEEDOR
                .requestMatchers("/proveedor/**").hasRole("PROVEEDOR")

                // Solo CLIENTE (y ADMIN también puede acceder)
                .requestMatchers("/perfil/**", "/pedidos/**", "/carrito/**").hasAnyRole("CLIENTE", "ADMIN")

                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")                         // Tu página de login personalizada
                .loginProcessingUrl("/login-process")        // URL que procesa el formulario
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(authSuccessHandler())        // Redirige según rol
                .failureUrl("/login?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // Mantener CSRF activo (recomendado para Thymeleaf)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Por si agregas API REST en el futuro
            );

        return http.build();
    }

    /**
     * Redirige al usuario según su rol después del login exitoso.
     * ROLE_ADMIN  -> /admin
     * ROLE_CLIENTE -> /
     */
    @Bean
    public AuthenticationSuccessHandler authSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isProveedor = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PROVEEDOR"));
            boolean isCliente = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

            if (isAdmin) {
                response.sendRedirect("/admin");
            } else if (isProveedor) {
                response.sendRedirect("/proveedor");
            } else if (isCliente) {
                response.sendRedirect("/");
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
