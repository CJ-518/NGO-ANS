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

    /**
     * Devuelve la entidad Usuario original. Los controladores la obtienen con @AuthenticationPrincipal.
     */
    public Usuario getUsuario() {
        return usuario;
    }

    /**
     * Autoridades del usuario: una sola, ROLE_<nombre del rol> (o ROLE_SIN_ROL si no tiene rol).
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String rol = usuario.getRol() != null ? usuario.getRol().getNombre() : "SIN_ROL";
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    /**
     * Hash BCrypt que Spring Security compara con la contraseña ingresada en el login.
     */
    @Override
    public String getPassword() {
        return usuario.getClave(); // hash BCrypt almacenado en la columna 'clave'
    }

    /**
     * El "nombre de usuario" de este sistema es el correo.
     */
    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    /**
     * Un usuario solo puede iniciar sesión si su estado es ACTIVO.
     */
    @Override
    public boolean isEnabled() {
        return "ACTIVO".equalsIgnoreCase(usuario.getEstado());
    }

    /**
     * Este sistema no maneja cuentas que expiran, cuentas bloqueadas ni credenciales vencidas:
     * los tres métodos (isAccountNonExpired, isAccountNonLocked, isCredentialsNonExpired) devuelven siempre true.
     */
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }
}
