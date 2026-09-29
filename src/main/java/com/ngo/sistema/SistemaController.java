package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * API REST general del panel: usuario actual, clientes, productos/garantía y solicitudes.
 * La asignación de técnicos está en {@link AsignacionController}, el seguimiento en
 * {@link SeguimientoController} y las ventas en {@link VentaController}.
 */
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
    private AsignacionRepository asignacionRepo;

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

    // Productos que este cliente tiene registrados (el cliente vive directamente en el producto),
    // para poder elegirlos directamente al cargar una solicitud.
    @GetMapping("/clientes/{id}/productos")
    public List<Producto> productosDeCliente(@PathVariable Long id) {
        return productoRepo.findByClienteIdClienteOrderByFechaVentaDesc(id);
    }

    // La garantía no se guarda aparte: se calcula siempre a partir de la fecha de venta del
    // producto (venta + 1 año), así que nunca puede quedar desincronizada.
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
        // Si dos solicitudes se crean al mismo instante pueden calcular el mismo número: la restricción
        // UNIQUE de la base rechaza la segunda y se reintenta con el número siguiente.
        for (int intento = 1; ; intento++) {
            solicitud.setNumero(siguienteNumeroSolicitud());
            try {
                return solicitudRepo.save(solicitud);
            } catch (DataIntegrityViolationException e) {
                if (intento >= 5) throw e;
            }
        }
    }

    /**
     * Genera el próximo número de solicitud: fecha de hoy (ddMMyyyy) + guion + orden del día
     * con al menos 2 dígitos (28092026-01, 28092026-02, ...). La secuencia arranca de nuevo cada día
     * y pasa de 99 sin problema (28092026-100).
     */
    private String siguienteNumeroSolicitud() {
        String prefijo = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + "-";
        int mayor = 0;
        for (String numero : solicitudRepo.findNumerosConPrefijo(prefijo)) {
            try {
                mayor = Math.max(mayor, Integer.parseInt(numero.substring(prefijo.length())));
            } catch (NumberFormatException e) {
                // número con otro formato: no cuenta para la secuencia del día
            }
        }
        return prefijo + String.format("%02d", mayor + 1);
    }

    // El TECNICO solo ve las solicitudes que tiene asignadas (su asignación vigente);
    // el resto de los roles ve todas.
    @GetMapping("/solicitudes")
    public List<Solicitud> listarSolicitudes(@AuthenticationPrincipal UsuarioPrincipal principal) {
        if (esTecnico(principal)) {
            return asignacionRepo.findSolicitudesAsignadasA(principal.getUsuario().getIdUsuario());
        }
        return solicitudRepo.findAll();
    }

    /**
     * GET /api/solicitudes/buscar?documento=...: solicitudes filtradas por documento del cliente
     * (coincidencia parcial). Con el documento vacío devuelve todas (o, para el TECNICO, las suyas).
     */
    @GetMapping("/solicitudes/buscar")
    public List<Solicitud> buscarPorDocumento(@RequestParam String documento,
                                              @AuthenticationPrincipal UsuarioPrincipal principal) {
        String doc = documento.trim();
        if (esTecnico(principal)) {
            return doc.isEmpty()
                    ? asignacionRepo.findSolicitudesAsignadasA(principal.getUsuario().getIdUsuario())
                    : asignacionRepo.buscarSolicitudesAsignadasA(principal.getUsuario().getIdUsuario(), doc);
        }
        if (doc.isEmpty()) {
            return solicitudRepo.findAll();
        }
        return solicitudRepo.findByClienteDocumentoContainingOrderByFechaDesc(doc);
    }

    // El TECNICO solo puede abrir una solicitud que tenga asignada (403 si no es suya).
    @GetMapping("/solicitudes/{id}")
    public ResponseEntity<?> obtenerSolicitud(@PathVariable Long id,
                                              @AuthenticationPrincipal UsuarioPrincipal principal) {
        Solicitud solicitud = solicitudRepo.findById(id).orElse(null);
        if (solicitud != null && esTecnico(principal) && !estaAsignadaA(id, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Esta solicitud no está asignada a tu usuario."));
        }
        return ResponseEntity.ok(solicitud);
    }

    /**
     * Indica si el usuario logueado tiene el rol TECNICO (que solo ve sus solicitudes asignadas).
     */
    private static boolean esTecnico(UsuarioPrincipal principal) {
        return principal.getUsuario().getRol() != null
                && "TECNICO".equals(principal.getUsuario().getRol().getNombre());
    }

    // La solicitud está asignada al usuario si su asignación vigente (la última) es la suya
    private boolean estaAsignadaA(Long idSolicitud, UsuarioPrincipal principal) {
        Asignacion vigente = asignacionRepo
                .findFirstBySolicitudIdSolicitudOrderByIdAsignacionDesc(idSolicitud).orElse(null);
        return vigente != null
                && vigente.getUsuario() != null
                && vigente.getUsuario().getIdUsuario().equals(principal.getUsuario().getIdUsuario());
    }

    /**
     * DELETE /api/solicitudes/{id}: solo ADMINISTRADOR. Borra la solicitud y, en cascada,
     * sus asignaciones y su seguimiento. Devuelve 204, o 404 si no existe.
     */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/solicitudes/{id}")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable Long id) {
        if (!solicitudRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        solicitudRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/solicitudes/{id}/estado: ADMINISTRADOR o ATENCION cambian el estado a mano (cuerpo: {"estado": "..."}).
     * Nota: si la solicitud no existe o falta "estado" en el cuerpo, no se cambia nada y se responde con
     * la solicitud (o null); el front solo revisa response.ok.
     */
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
