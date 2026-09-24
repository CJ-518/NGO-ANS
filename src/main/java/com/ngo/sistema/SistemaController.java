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

    // El VENDEDOR también registra clientes: los carga desde el Portal de Ventas.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENCION', 'VENDEDOR')")
    @PostMapping("/clientes")
    public Cliente crearCliente(@RequestBody Cliente cliente) {
        return clienteRepo.save(cliente);
    }

    // Actualiza los datos de un cliente ya existente (el documento no se toca: es la clave de búsqueda).
    // Se usa cuando "Nueva solicitud" encontró al cliente por documento y el usuario corrigió algún dato.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENCION', 'VENDEDOR')")
    @PutMapping("/clientes/{id}")
    public ResponseEntity<Cliente> actualizarCliente(@PathVariable Long id, @RequestBody Cliente datos) {
        return clienteRepo.findById(id).map(cliente -> {
            cliente.setNombre(datos.getNombre());
            cliente.setTelefono(datos.getTelefono());
            cliente.setCorreo(datos.getCorreo());
            return ResponseEntity.ok(clienteRepo.save(cliente));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Busca un cliente por documento exacto, para autocompletar sus datos en "Nueva solicitud".
    // 404 si no existe (significa que es un cliente nuevo).
    @GetMapping("/clientes/buscar")
    public ResponseEntity<Cliente> buscarClientePorDocumento(@RequestParam String documento) {
        return clienteRepo.findByDocumento(documento.trim())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Listado de clientes con al menos un producto registrado, para las sugerencias de
    // documento en "Nueva solicitud" (se filtra en el momento en que se escribe).
    // Un cliente sin productos no sirve acá: no hay nada que atender en una solicitud.
    @GetMapping("/clientes")
    public List<Cliente> listarClientes() {
        return clienteRepo.findClientesConProductos();
    }

    // Productos que este cliente ya tiene registrados (ya no se busca a través de garantías
    // sueltas: el cliente vive directamente en el producto), para poder elegirlos directamente
    // al cargar una solicitud en vez de tener que buscarlos de nuevo.
    @GetMapping("/clientes/{id}/productos")
    public List<Producto> productosDeCliente(@PathVariable Long id) {
        return productoRepo.findByClienteIdClienteOrderByFechaVentaDesc(id);
    }

    @GetMapping("/productos")
    public List<Producto> listarProductos() {
        return productoRepo.findAll();
    }

    // La garantía ya no se guarda aparte: se calcula siempre a partir de la fecha de venta del
    // producto (venta + 1 año), así que nunca puede quedar desincronizada como pasaba antes.
    @GetMapping("/productos/{id}/garantia")
    public ResponseEntity<?> obtenerGarantiaDeProducto(@PathVariable Long id) {
        Producto producto = productoRepo.findById(id).orElse(null);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "idProducto", producto.getIdProducto(),
                "idCliente", producto.getCliente().getIdCliente(),
                "fechaInicio", producto.getFechaVenta(),
                "fechaFin", producto.getFechaFinGarantia(),
                "estado", producto.isGarantiaVigente() ? "VIGENTE" : "VENCIDA"
        ));
    }

    // TECNICO no puede registrar solicitudes nuevas.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENCION')")
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
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENCION')")
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
