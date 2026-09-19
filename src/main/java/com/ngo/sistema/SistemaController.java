package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SistemaController {

    @Autowired
    private ClienteRepository clienteRepo;
    
    @Autowired
    private ProductoRepository productoRepo;
    
    @Autowired
    private SolicitudRepository solicitudRepo;

    @PostMapping("/clientes")
    public Cliente crearCliente(@RequestBody Cliente cliente) {
        return clienteRepo.save(cliente);
    }

    @PostMapping("/productos")
    public Producto crearProducto(@RequestBody Producto producto) {
        return productoRepo.save(producto);
    }

    @PostMapping("/solicitudes")
    public Solicitud crearSolicitud(@RequestBody Solicitud solicitud) {
        return solicitudRepo.save(solicitud);
    }

    @GetMapping("/solicitudes")
    public List<Solicitud> listarSolicitudes() {
        return solicitudRepo.findAll();
    }

    @GetMapping("/solicitudes/buscar")
    public List<Solicitud> buscarPorDocumento(@RequestParam String documento) {
        String doc = documento.trim();
        if (doc.isEmpty()) {
            return solicitudRepo.findAll();
        }
        return solicitudRepo.findByClienteDocumentoContainingOrderByFechaDesc(doc);
    }

    @GetMapping("/solicitudes/{id}")
    public Solicitud obtenerSolicitud(@PathVariable Long id) {
        return solicitudRepo.findById(id).orElse(null);
    }

    @DeleteMapping("/solicitudes/{id}")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable Long id) {
        if (!solicitudRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        solicitudRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/solicitudes/{id}/estado")
    public Solicitud actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Solicitud solicitud = solicitudRepo.findById(id).orElse(null);
        if (solicitud != null && payload.containsKey("estado")) {
            solicitud.setEstadoActual(payload.get("estado"));
            solicitudRepo.save(solicitud);
        }
        return solicitud;
    }
}