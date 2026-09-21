package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Autowired
    private GarantiaRepository garantiaRepo;

    // Identidad del usuario logueado: id, nombre, correo y rol, para que app.js
    // pinte el header y muestre/oculte botones según el rol (y sepa cuáles son "sus" solicitudes).
    @GetMapping("/usuario/actual")
    public Map<String, Object> usuarioActual(@AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario u = principal.getUsuario();
        return Map.of(
                "idUsuario", u.getIdUsuario(),
                "nombre", u.getNombre(),
                "correo", u.getCorreo(),
                "rol", u.getRol().getNombre()
        );
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FUNCIONARIO')")
    @PostMapping("/clientes")
    public Cliente crearCliente(@RequestBody Cliente cliente) {
        return clienteRepo.save(cliente);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FUNCIONARIO')")
    @PostMapping("/productos")
    public Producto crearProducto(@RequestBody Producto producto) {
        return productoRepo.save(producto);
    }

    @GetMapping("/productos")
    public List<Producto> listarProductos() {
        return productoRepo.findAll();
    }

    @GetMapping("/productos/{id}/garantia")
    public ResponseEntity<Garantia> obtenerGarantiaDeProducto(@PathVariable Long id) {
        return garantiaRepo.findFirstByProductoIdProductoOrderByFechaFinDesc(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // TECNICO no puede registrar solicitudes nuevas.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FUNCIONARIO')")
    @PostMapping("/solicitudes")
    public Solicitud crearSolicitud(@RequestBody Solicitud solicitud) {
        if (solicitud.getGarantia() == null
                && solicitud.getProducto() != null
                && solicitud.getProducto().getIdProducto() != null) {
            garantiaRepo.findFirstByProductoIdProductoOrderByFechaFinDesc(solicitud.getProducto().getIdProducto())
                    .ifPresent(solicitud::setGarantia);
        }
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

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/solicitudes/{id}")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable Long id) {
        if (!solicitudRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        solicitudRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // TECNICO no puede cambiar el estado de una solicitud.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FUNCIONARIO')")
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