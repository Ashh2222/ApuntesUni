package com.university.website.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .userDetailsService(customUserDetailsService)
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas y recursos estáticos
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login",
                    "/registro",
                    "/recuperar-password",
                    "/buscar",
                    "/apuntes",
                    "/apuntes/detalle/**",
                    "/apuntes/{id}",
                    "/apuntes/{id}/vista-previa",
                    "/css/**",
                    "/js/**",
                    "/assets/**",
                    "/favicon.ico",
                    "/h2-console/**",
                    "/actuator/health"
                ).permitAll()

                // Panel de administración
                .requestMatchers("/admin/**").hasRole("ADMINISTRATOR")

                // Panel de moderación y reportes
                .requestMatchers("/moderacion/**").hasAnyRole("MODERATOR", "ADMINISTRATOR")

                // Acciones de usuarios autenticados
                .requestMatchers(
                    "/dashboard",
                    "/subir",
                    "/apuntes/subir",
                    "/apuntes/*/descargar",
                    "/apuntes/*/valorar",
                    "/apuntes/*/comentar",
                    "/apuntes/*/favorito",
                    "/apuntes/*/reportar",
                    "/onboarding/**",
                    "/mis-materias/**",
                    "/perfil/**",
                    "/configuracion/**",
                    "/notificaciones/**"
                ).authenticated()

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
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
            .csrf(csrf -> csrf
                // Excluir h2-console de CSRF
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .addHeaderWriter(new XFrameOptionsHeaderWriter(
                    XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN
                ))
            );

        return http.build();
    }
}
