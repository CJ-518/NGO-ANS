package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GarantiaRepository extends JpaRepository<Garantia, Long> {

    // Garantía más reciente de un producto (la que termina más tarde)
    Optional<Garantia> findFirstByProductoIdProductoOrderByFechaFinDesc(Long idProducto);
}