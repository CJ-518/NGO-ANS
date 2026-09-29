package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link Solicitud}.
 */
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    // Busca por documento del cliente (coincidencia parcial), las más recientes primero
    List<Solicitud> findByClienteDocumentoContainingOrderByFechaDesc(String documento);

    // Para el link público de seguimiento (sin login): busca por el código, no por el id
    Optional<Solicitud> findByCodigoPublico(String codigoPublico);

    // Números ya emitidos que empiezan con un prefijo (ej. "28092026-"): sirven para calcular
    // cuál es el siguiente número de ese día.
    @Query("select s.numero from Solicitud s where s.numero like concat(:prefijo, '%')")
    List<String> findNumerosConPrefijo(@Param("prefijo") String prefijo);
}
