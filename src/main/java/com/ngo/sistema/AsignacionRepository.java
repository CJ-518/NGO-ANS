package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Solicitudes que tiene asignadas un técnico: aquellas cuya asignación vigente (la última)
    // es de ese usuario. Si una solicitud se reasigna a otro técnico, deja de aparecerle.
    @Query("SELECT a.solicitud FROM Asignacion a WHERE a.usuario.idUsuario = :idUsuario " +
           "AND a.idAsignacion = (SELECT MAX(a2.idAsignacion) FROM Asignacion a2 WHERE a2.solicitud = a.solicitud) " +
           "ORDER BY a.solicitud.idSolicitud")
    List<Solicitud> findSolicitudesAsignadasA(@Param("idUsuario") Long idUsuario);

    // Igual que la anterior, filtrando por documento del cliente (coincidencia parcial),
    // las más recientes primero.
    @Query("SELECT a.solicitud FROM Asignacion a WHERE a.usuario.idUsuario = :idUsuario " +
           "AND a.idAsignacion = (SELECT MAX(a2.idAsignacion) FROM Asignacion a2 WHERE a2.solicitud = a.solicitud) " +
           "AND a.solicitud.cliente.documento LIKE CONCAT('%', :documento, '%') " +
           "ORDER BY a.solicitud.fecha DESC")
    List<Solicitud> buscarSolicitudesAsignadasA(@Param("idUsuario") Long idUsuario,
                                                @Param("documento") String documento);
}
