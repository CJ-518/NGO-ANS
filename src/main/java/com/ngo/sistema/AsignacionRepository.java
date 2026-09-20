package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    // Última asignación de una solicitud (la vigente)
    Optional<Asignacion> findFirstBySolicitudIdSolicitudOrderByIdAsignacionDesc(Long idSolicitud);
}