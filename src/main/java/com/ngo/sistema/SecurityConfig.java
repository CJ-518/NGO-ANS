package com.ngo.sistema;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize en el controller
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Prototipo académico: la API la consume el mismo front (fetch same-origin),
            // no hay clientes externos todavía, así que se desactiva CSRF para simplificar.
            // Antes de producción esto debe reactivarse y manejar el token en app.js.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos: página de login y estáticos que ella necesita
                .requestMatchers("/login.html", "/login", "/style.css", "/logo.png").permitAll()

                // Sólo ADMINISTRADOR puede eliminar solicitudes o administrar usuarios/roles
                .requestMatchers(HttpMethod.DELETE, "/api/solicitudes/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/usuarios.html", "/usuarios.js").hasRole("ADMINISTRADOR")

                // TECNICO no puede dar de alta clientes/productos/solicitudes nuevas,
                // ni cambiar el estado de una solicitud: eso queda para ADMINISTRADOR y FUNCIONARIO.
                .requestMatchers(HttpMethod.POST, "/api/clientes").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                .requestMatchers(HttpMethod.POST, "/api/productos").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                .requestMatchers(HttpMethod.POST, "/api/solicitudes").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/estado").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                // Asignar técnico y registrar diagnóstico: ADMINISTRADOR o FUNCIONARIO
                .requestMatchers(HttpMethod.POST, "/api/solicitudes/*/asignar").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                .requestMatchers(HttpMethod.POST, "/api/solicitudes/*/diagnostico").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO", "TECNICO")

                // Todo lo demás (panel, resto de la API de lectura) requiere estar logueado
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index.html", true)
                .failureUrl("/login.html?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.html?logout")
                .permitAll()
            );

        return http.build();
    }
}