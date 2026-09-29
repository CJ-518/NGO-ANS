package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Acceso a datos de {@link Articulo}. Spring Data genera la implementación en tiempo de ejecución;
 * los métodos heredados de JpaRepository (save, findById, findAll, ...) ya vienen incluidos.
 */
public interface ArticuloRepository extends JpaRepository<Articulo, Long> {

    // Catálogo visible en el portal de ventas: solo artículos activos
    List<Articulo> findByEstadoOrderByNombreAsc(String estado);
}
