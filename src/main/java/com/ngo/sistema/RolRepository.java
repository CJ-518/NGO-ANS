package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Acceso a datos de {@link Rol}.
 */
public interface RolRepository extends JpaRepository<Rol, Long> {
    /**
     * Busca un rol por su nombre exacto (por ejemplo "TECNICO"). Lo usa UsuarioController al dar de alta un usuario.
     */
    Optional<Rol> findByNombre(String nombre);
}
