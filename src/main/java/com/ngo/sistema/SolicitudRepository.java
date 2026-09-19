package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    // Busca por documento del cliente (coincidencia parcial), las más recientes primero
    List<Solicitud> findByClienteDocumentoContainingOrderByFechaDesc(String documento);
}