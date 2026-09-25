package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    // Busca por documento del cliente (coincidencia parcial), las más recientes primero
    List<Solicitud> findByClienteDocumentoContainingOrderByFechaDesc(String documento);

    // Para el link público de seguimiento (sin login): busca por el código, no por el id
    Optional<Solicitud> findByCodigoPublico(String codigoPublico);
}
