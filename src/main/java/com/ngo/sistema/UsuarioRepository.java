package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link Usuario}.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    /**
     * Busca un usuario por su correo exacto. Lo usa {@link CustomUserDetailsService} en el login.
     */
    Optional<Usuario> findByCorreo(String correo);

    // Para evitar correos repetidos aunque cambien las mayúsculas
    boolean existsByCorreoIgnoreCase(String correo);

    // Usuarios de un rol en un estado dado (por ejemplo, los TECNICO que están ACTIVO)
    List<Usuario> findByRolNombreAndEstadoOrderByNombreAsc(String rol, String estado);
}
