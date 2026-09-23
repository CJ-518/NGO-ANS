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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize en el controller
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Redirección post-login según el rol: el VENDEDOR entra directo al Portal de Ventas
    // (no ve el panel de garantías/servicio técnico); el resto va al panel general.
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean esVendedor = authentication.getAuthorities().stream()
                    .anyMatch(autoridad -> "ROLE_VENDEDOR".equals(autoridad.getAuthority()));
            String destino = esVendedor ? "/portal-ventas.html" : "/index.html";
            response.sendRedirect(request.getContextPath() + destino);
        };
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

                // Portal de ventas: lo usan el VENDEDOR y el ADMINISTRADOR
                .requestMatchers("/portal-ventas.html", "/portal-ventas.js").hasAnyRole("VENDEDOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/ventas").hasAnyRole("VENDEDOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/ventas").hasAnyRole("VENDEDOR", "ADMINISTRADOR")
                // Alta de artículos y corrección de stock: solo ADMINISTRADOR
                .requestMatchers(HttpMethod.POST, "/api/articulos").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT, "/api/articulos/*/stock").hasRole("ADMINISTRADOR")

                // TECNICO no puede dar de alta clientes/productos/solicitudes nuevas,
                // ni cambiar el estado de cualquier solicitud: eso queda para ADMINISTRADOR y ATENCION.
                // (El TECNICO solo avanza las solicitudes que tiene asignadas: /diagnostico y /finalizar.)
                // El VENDEDOR también puede registrar clientes: los carga desde el Portal de Ventas.
                .requestMatchers(HttpMethod.POST, "/api/clientes").hasAnyRole("ADMINISTRADOR", "ATENCION", "VENDEDOR")
                .requestMatchers(HttpMethod.POST, "/api/productos").hasAnyRole("ADMINISTRADOR", "ATENCION")
                .requestMatchers(HttpMethod.POST, "/api/solicitudes").hasAnyRole("ADMINISTRADOR", "ATENCION")
                .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/estado").hasAnyRole("ADMINISTRADOR", "ATENCION")

                // Asignar técnico: ADMINISTRADOR o ATENCION
                .requestMatchers(HttpMethod.POST, "/api/solicitudes/*/asignar").hasAnyRole("ADMINISTRADOR", "ATENCION")

                // Registrar diagnóstico y finalizar: solo el TECNICO (el controlador además exige que
                // la solicitud esté asignada a ese técnico)
                .requestMatchers(HttpMethod.POST, "/api/solicitudes/*/diagnostico").hasRole("TECNICO")
                .requestMatchers(HttpMethod.POST, "/api/solicitudes/*/finalizar").hasRole("TECNICO")

                // Todo lo demás (panel, resto de la API de lectura) requiere estar logueado
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler())
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