package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
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

    // Listado completo de clientes, para las sugerencias de documento en "Nueva solicitud"
    // (el navegador filtra solo, a medida que se escribe, con un <datalist>).
    @GetMapping("/clientes")
    public List<Cliente> listarClientes() {
        return clienteRepo.findAll();
    }

    // Productos que este cliente ya tiene registrados (ya no se busca a través de garantías
    // sueltas: el cliente vive directamente en el producto), para poder elegirlos directamente
    // al cargar una solicitud en vez de tener que buscarlos de nuevo.
    @GetMapping("/clientes/{id}/productos")
    public List<Producto> productosDeCliente(@PathVariable Long id) {
        return productoRepo.findByClienteIdClienteOrderByFechaVentaDesc(id);
    }

    // El sistema de "productos sin cliente" ya no está en uso: todo producto representa una
    // venta concreta, así que exige el cliente que lo compró y la fecha de venta (por defecto,
    // hoy). La garantía (venta + 1 año) se calcula sola a partir de esa fecha.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENCION')")
    @PostMapping("/productos")
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        if (producto.getCliente() == null || producto.getCliente().getIdCliente() == null) {
            return error(HttpStatus.BAD_REQUEST,
                    "El producto debe tener el cliente que lo compró: ya no se admiten productos sin cliente.");
        }
        Cliente cliente = clienteRepo.findById(producto.getCliente().getIdCliente()).orElse(null);
        if (cliente == null) {
            return error(HttpStatus.BAD_REQUEST, "El cliente indicado no existe.");
        }
        if (producto.getMarca() == null || producto.getMarca().isBlank()
                || producto.getModelo() == null || producto.getModelo().isBlank()
                || producto.getNroSerie() == null || producto.getNroSerie().isBlank()
                || producto.getTipoProducto() == null || producto.getTipoProducto().isBlank()) {
            return error(HttpStatus.BAD_REQUEST, "Marca, modelo, número de serie y tipo de producto son obligatorios.");
        }
        if (productoRepo.findByNroSerie(producto.getNroSerie().trim()).isPresent()) {
            return error(HttpStatus.CONFLICT, "Ya existe un producto registrado con ese número de serie.");
        }
        if (producto.getFechaVenta() == null) {
            producto.setFechaVenta(LocalDate.now());
        }
        producto.setCliente(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoRepo.save(producto));
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

    private static ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }
}