package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GarantiaRepository extends JpaRepository<Garantia, Long> {

    // Garantía más reciente de un producto (la que termina más tarde)
    Optional<Garantia> findFirstByProductoIdProductoOrderByFechaFinDesc(Long idProducto);

    // Todas las garantías (= productos comprados) de un cliente, de la más nueva a la más vieja.
    // Se usa para ofrecer en "Nueva solicitud" los productos que ese cliente ya tiene registrados.
    List<Garantia> findByClienteIdClienteOrderByFechaFinDesc(Long idCliente);
}