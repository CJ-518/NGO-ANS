package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Puente entre Spring Security y la tabla usuario: cuando alguien inicia sesión, Spring Security
 * llama a este servicio para cargar al usuario a partir del "username" del formulario (que en este
 * sistema es el CORREO).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    /**
     * Busca al usuario por correo y lo adapta a {@link UsuarioPrincipal}.
     * Si no existe lanza UsernameNotFoundException, que Spring Security traduce en un login fallido
     * (redirige a /login.html?error).
     */
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepo.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
        return new UsuarioPrincipal(usuario);
    }
}
