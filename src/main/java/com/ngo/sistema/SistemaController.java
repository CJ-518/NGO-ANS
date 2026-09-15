package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
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
    @GetMapping("/solicitudes/{id}")
    public Solicitud obtenerSolicitud(@PathVariable Long id) {
        return solicitudRepo.findById(id).orElse(null);
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