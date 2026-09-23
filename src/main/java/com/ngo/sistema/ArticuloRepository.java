package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticuloRepository extends JpaRepository<Articulo, Long> {

    // Catálogo visible en el portal de ventas: solo artículos activos
    List<Articulo> findByEstadoOrderByNombreAsc(String estado);
}
