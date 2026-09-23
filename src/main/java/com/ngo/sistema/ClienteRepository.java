package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Se usa para autocompletar los datos del cliente en "Nueva solicitud" y en el Portal de Ventas.
    Optional<Cliente> findByDocumento(String documento);

    // Solo los clientes que tienen al menos un producto registrado: son los únicos que se
    // pueden usar en "Nueva solicitud" (sin producto no hay nada que atender). Evita que
    // clientes con una venta antigua pero sin producto generado aparezcan como sugerencia.
    @Query("SELECT c FROM Cliente c WHERE EXISTS (SELECT 1 FROM Producto p WHERE p.cliente = c)")
    List<Cliente> findClientesConProductos();
}