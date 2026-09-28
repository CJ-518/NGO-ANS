package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Ya no se busca a través de Garantia: el cliente vive directamente en el producto.
    // Se usa en "Nueva solicitud" para ofrecer los productos que ese cliente ya compró.
    List<Producto> findByClienteIdClienteOrderByFechaVentaDesc(Long idCliente);

    // N° de serie con formato SN-XX-0000 de los productos de un tipo: sirven para reutilizar
    // el prefijo (XX) que ya usa ese tipo de producto al generar el serial de una venta nueva.
    @Query("select p.nroSerie from Producto p where p.tipoProducto = :tipo and p.nroSerie like 'SN-%'")
    List<String> findNroSerieDeTipo(@Param("tipo") String tipo);

    // Todos los N° de serie que empiezan con un prefijo (ej. \"SN-AA-\"), para calcular el siguiente número.
    @Query("select p.nroSerie from Producto p where p.nroSerie like concat(:prefijo, '%')")
    List<String> findNroSerieConPrefijo(@Param("prefijo") String prefijo);
}
