package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Acceso a datos de {@link Reclamo} (reclamos de los clientes por la demora en la atención).
 */
public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {

    // Último reclamo de una solicitud (el más reciente)
    Optional<Reclamo> findFirstBySolicitudIdSolicitudOrderByIdReclamoDesc(Long idSolicitud);
}
