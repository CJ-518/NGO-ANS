package com.ngo.sistema;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Se usa para autocompletar los datos del cliente en "Nueva solicitud" y en el Portal de Ventas.
    Optional<Cliente> findByDocumento(String documento);
}