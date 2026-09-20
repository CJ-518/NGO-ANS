package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    // Para evitar correos repetidos aunque cambien las mayúsculas
    boolean existsByCorreoIgnoreCase(String correo);

    // Usuarios de un rol en un estado dado (por ejemplo, los TECNICO que están ACTIVO)
    List<Usuario> findByRolNombreAndEstadoOrderByNombreAsc(String rol, String estado);
}