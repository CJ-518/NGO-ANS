package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    // Última asignación de una solicitud (la vigente)
    Optional<Asignacion> findFirstBySolicitudIdSolicitudOrderByIdAsignacionDesc(Long idSolicitud);

    // Cantidad de solicitudes pendientes (no FINALIZADAS) por técnico. Solo cuenta la asignación
    // vigente de cada solicitud (la última), no el historial. Cada fila: [idUsuario, cantidad].
    @Query("SELECT a.usuario.idUsuario, COUNT(a) FROM Asignacion a " +
           "WHERE a.idAsignacion = (SELECT MAX(a2.idAsignacion) FROM Asignacion a2 WHERE a2.solicitud = a.solicitud) " +
           "AND a.solicitud.estadoActual <> 'FINALIZADA' " +
           "GROUP BY a.usuario.idUsuario")
    List<Object[]> contarPendientesPorTecnico();
}
