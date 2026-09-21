package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    // Historial de una solicitud, del registro más antiguo al más reciente
    List<Seguimiento> findBySolicitudIdSolicitudOrderByIdSeguimientoAsc(Long idSolicitud);
}