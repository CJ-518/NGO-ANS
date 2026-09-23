package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Ya no se busca a través de Garantia: el cliente vive directamente en el producto.
    // Se usa en "Nueva solicitud" para ofrecer los productos que ese cliente ya compró.
    List<Producto> findByClienteIdClienteOrderByFechaVentaDesc(Long idCliente);

    Optional<Producto> findByNroSerie(String nroSerie);
}