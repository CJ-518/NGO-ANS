package com.ngo.sistema;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapta la entidad Usuario (existente en el modelo) al contrato UserDetails
 * que necesita Spring Security para autenticar y autorizar.
 *
 * El rol se expone como ROLE_<nombre> porque hasRole("X") en la configuración
 * busca automáticamente la autoridad "ROLE_X".
 */
public class UsuarioPrincipal implements UserDetails {

    private final Usuario usuario;

    public UsuarioPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String rol = usuario.getRol() != null ? usuario.getRol().getNombre() : "SIN_ROL";
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public String getPassword() {
        return usuario.getClave(); // hash BCrypt almacenado en la columna 'clave'
    }

    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVO".equalsIgnoreCase(usuario.getEstado());
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }
}