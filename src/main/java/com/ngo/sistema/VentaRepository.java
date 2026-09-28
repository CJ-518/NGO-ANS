package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    // El VENDEDOR solo ve sus propias ventas
    List<Venta> findByVendedorIdUsuarioOrderByFechaDesc(Long idUsuario);

    // El ADMINISTRADOR ve todas
    List<Venta> findAllByOrderByFechaDesc();

    // Facturas (ventas) de un cliente, las más recientes primero. Se usan en "Nueva solicitud".
    List<Venta> findByClienteIdClienteOrderByFechaDesc(Long idCliente);
}
