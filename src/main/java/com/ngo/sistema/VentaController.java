package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Portal de ventas: catálogo de artículos con stock, y registro de ventas.
 * Pueden vender el VENDEDOR y el ADMINISTRADOR (SecurityConfig también protege
 * POST /api/ventas). El alta y ajuste de stock de artículos queda reservado al
 * ADMINISTRADOR.
 */
@RestController
@RequestMapping("/api")
public class VentaController {

    /** Un renglón del carrito: qué artículo y cuánta cantidad. */
    public record ItemVenta(Long idArticulo, Integer cantidad) {}

    /** Cuerpo del pedido de venta. El cliente es opcional (venta a consumidor final). */
    public record NuevaVenta(Long idCliente, List<ItemVenta> items) {}

    /** Cuerpo para ajustar el stock de un artículo existente. */
    public record AjusteStock(Integer stock) {}

    @Autowired
    private ArticuloRepository articuloRepo;

    @Autowired
    private VentaRepository ventaRepo;

    @Autowired
    private ClienteRepository clienteRepo;

    @Autowired
    private ProductoRepository productoRepo;

    // ---------- Catálogo / stock ----------

    // Cualquier usuario autenticado puede consultar el catálogo (lo necesita el portal de ventas)
    @GetMapping("/articulos")
    public List<Articulo> listarArticulos() {
        return articuloRepo.findByEstadoOrderByNombreAsc("ACTIVO");
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/articulos")
    public ResponseEntity<?> crearArticulo(@RequestBody Articulo articulo) {
        if (articulo.getNombre() == null || articulo.getNombre().isBlank()) {
            return error(HttpStatus.BAD_REQUEST, "El nombre del artículo es obligatorio.");
        }
        if (articulo.getPrecio() == null || articulo.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            return error(HttpStatus.BAD_REQUEST, "El precio debe ser un número válido y no negativo.");
        }
        if (articulo.getStock() == null || articulo.getStock() < 0) {
            articulo.setStock(0);
        }
        articulo.setEstado("ACTIVO");
        return ResponseEntity.status(HttpStatus.CREATED).body(articuloRepo.save(articulo));
    }

    // Corrige/repone el stock de un artículo (por ejemplo, tras recibir mercadería nueva)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/articulos/{id}/stock")
    public ResponseEntity<?> ajustarStock(@PathVariable Long id, @RequestBody AjusteStock datos) {
        if (datos.stock() == null || datos.stock() < 0) {
            return error(HttpStatus.BAD_REQUEST, "El stock debe ser un número mayor o igual a 0.");
        }
        Articulo articulo = articuloRepo.findById(id).orElse(null);
        if (articulo == null) {
            return error(HttpStatus.NOT_FOUND, "El artículo no existe.");
        }
        articulo.setStock(datos.stock());
        return ResponseEntity.ok(articuloRepo.save(articulo));
    }

    // ---------- Ventas ----------

    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')")
    @GetMapping("/ventas")
    public List<Venta> listarVentas(@AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuario = principal.getUsuario();
        if ("ADMINISTRADOR".equals(usuario.getRol().getNombre())) {
            return ventaRepo.findAllByOrderByFechaDesc();
        }
        // El VENDEDOR solo ve las ventas que él mismo registró
        return ventaRepo.findByVendedorIdUsuarioOrderByFechaDesc(usuario.getIdUsuario());
    }

    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')")
    @PostMapping("/ventas")
    @Transactional
    public ResponseEntity<?> registrarVenta(@RequestBody NuevaVenta datos,
                                             @AuthenticationPrincipal UsuarioPrincipal principal) {
        if (datos.items() == null || datos.items().isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "La venta debe tener al menos un artículo.");
        }

        Venta venta = new Venta();
        venta.setVendedor(principal.getUsuario());

        if (datos.idCliente() != null) {
            Cliente cliente = clienteRepo.findById(datos.idCliente()).orElse(null);
            if (cliente == null) {
                return error(HttpStatus.BAD_REQUEST, "El cliente indicado no existe.");
            }
            venta.setCliente(cliente);
        }

        BigDecimal total = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (ItemVenta item : datos.items()) {
            if (item.idArticulo() == null || item.cantidad() == null || item.cantidad() <= 0) {
                return error(HttpStatus.BAD_REQUEST, "Cada artículo necesita una cantidad válida (mayor a 0).");
            }

            Articulo articulo = articuloRepo.findById(item.idArticulo()).orElse(null);
            if (articulo == null || !"ACTIVO".equals(articulo.getEstado())) {
                return error(HttpStatus.BAD_REQUEST, "Uno de los artículos ya no está disponible.");
            }
            if (articulo.getStock() < item.cantidad()) {
                return error(HttpStatus.CONFLICT,
                        "Stock insuficiente de \"" + articulo.getNombre() + "\" (disponible: " + articulo.getStock() + ").");
            }

            // Se descuenta el stock dentro de la misma transacción que crea la venta
            articulo.setStock(articulo.getStock() - item.cantidad());
            articuloRepo.save(articulo);

            BigDecimal subtotal = articulo.getPrecio().multiply(BigDecimal.valueOf(item.cantidad()));
            total = total.add(subtotal);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setArticulo(articulo);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(articulo.getPrecio());
            detalle.setSubtotal(subtotal);
            detalles.add(detalle);
        }

        venta.setTotal(total);
        venta.setDetalles(detalles);
        venta = ventaRepo.save(venta);

        // Se registra cada unidad vendida como un Producto (equipo con N° de serie),
        // para que aparezca en "Nueva solicitud" y pueda tener seguimiento de garantía.
        // Solo tiene sentido si la venta tiene un cliente (un producto siempre necesita dueño).
        if (venta.getCliente() != null) {
            registrarProductosDeLaVenta(venta, detalles);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    // Genera un Producto por cada unidad vendida, con un N° de serie propio (no es el de
    // fábrica: el catálogo de artículos no lo captura). El formato VTA-{idVenta}-{idArticulo}-{n}
    // garantiza que sea único y permite rastrear de qué venta salió cada equipo.
    private void registrarProductosDeLaVenta(Venta venta, List<DetalleVenta> detalles) {
        for (DetalleVenta detalle : detalles) {
            Articulo articulo = detalle.getArticulo();
            String tipoProducto = articulo.getNombre();
            String marca = tieneTexto(articulo.getCategoria()) ? articulo.getCategoria() : tipoProducto;
            String modelo = tieneTexto(articulo.getDescripcion()) ? articulo.getDescripcion() : tipoProducto;

            for (int unidad = 1; unidad <= detalle.getCantidad(); unidad++) {
                Producto producto = new Producto();
                producto.setTipoProducto(tipoProducto);
                producto.setMarca(marca);
                producto.setModelo(modelo);
                producto.setNroSerie("VTA-" + venta.getIdVenta() + "-" + articulo.getIdArticulo() + "-" + unidad);
                producto.setCliente(venta.getCliente());
                producto.setFechaVenta(LocalDate.now());
                productoRepo.save(producto);
            }
        }
    }

    private static boolean tieneTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }
}