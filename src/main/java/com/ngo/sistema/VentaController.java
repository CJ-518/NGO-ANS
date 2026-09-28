package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Portal de ventas: catálogo de artículos con stock, y registro de ventas.
 * Pueden vender el VENDEDOR y el ADMINISTRADOR (SecurityConfig también protege
 * POST /api/ventas). El alta de artículos queda reservada al ADMINISTRADOR.
 */
@RestController
@RequestMapping("/api")
public class VentaController {

    /** Un renglón del carrito: qué artículo y cuánta cantidad. */
    public record ItemVenta(Long idArticulo, Integer cantidad) {}

    /** Cuerpo del pedido de venta. El cliente es opcional (venta a consumidor final). */
    public record NuevaVenta(Long idCliente, List<ItemVenta> items) {}

    /** Resumen de la factura de la compra de un producto (se muestra en "Nueva solicitud"). */
    public record FacturaResumen(Long idVenta, LocalDateTime fecha, BigDecimal total, String detalle) {}

    // Formato único de N° de serie de todo producto: SN-{prefijo del tipo}-{número de 4 dígitos}
    private static final Pattern SERIE_ESTANDAR = Pattern.compile("^SN-([A-Z]{2})-(\\d+)$");

    @Autowired
    private ArticuloRepository articuloRepo;

    @Autowired
    private VentaRepository ventaRepo;

    @Autowired
    private ClienteRepository clienteRepo;

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private FacturaService facturaService;

    // ---------- Catálogo ----------

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
        // Tipo (categoría), marca y modelo son obligatorios: de ahí salen los datos de cada unidad vendida
        if (!tieneTexto(articulo.getCategoria()) || !tieneTexto(articulo.getMarca()) || !tieneTexto(articulo.getModelo())) {
            return error(HttpStatus.BAD_REQUEST, "La categoría (tipo de producto), la marca y el modelo son obligatorios.");
        }
        articulo.setNombre(articulo.getNombre().trim());
        articulo.setCategoria(articulo.getCategoria().trim());
        articulo.setMarca(articulo.getMarca().trim());
        articulo.setModelo(articulo.getModelo().trim());
        if (articulo.getCategoria().length() > 50 || articulo.getMarca().length() > 50 || articulo.getModelo().length() > 50) {
            return error(HttpStatus.BAD_REQUEST, "La categoría, la marca y el modelo admiten hasta 50 caracteres.");
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
                return errorConReversion(HttpStatus.BAD_REQUEST, "Cada artículo necesita una cantidad válida (mayor a 0).");
            }

            Articulo articulo = articuloRepo.findById(item.idArticulo()).orElse(null);
            if (articulo == null || !"ACTIVO".equals(articulo.getEstado())) {
                return errorConReversion(HttpStatus.BAD_REQUEST, "Uno de los artículos ya no está disponible.");
            }
            // Si se vende a un cliente, cada unidad se registra como producto: el artículo tiene que
            // tener tipo, marca y modelo para que el producto quede igual que los demás.
            if (venta.getCliente() != null && !datosDeProductoCompletos(articulo)) {
                return errorConReversion(HttpStatus.CONFLICT,
                        "Al artículo \"" + articulo.getNombre() + "\" le faltan la categoría, la marca o el modelo. Completalos antes de venderlo a un cliente.");
            }
            if (articulo.getStock() < item.cantidad()) {
                return errorConReversion(HttpStatus.CONFLICT,
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

    // Factura de la compra de UN producto (la que se muestra en "Nueva solicitud" al elegirlo):
    // la venta de la que salió ese producto. La ven ADMINISTRADOR y ATENCION, que son quienes
    // registran solicitudes. 404 si el producto no existe o no tiene venta asociada.
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENCION')")
    @GetMapping("/productos/{id}/factura")
    public ResponseEntity<?> facturaDelProducto(@PathVariable Long id) {
        Producto producto = productoRepo.findById(id).orElse(null);
        if (producto == null) {
            return error(HttpStatus.NOT_FOUND, "El producto no existe.");
        }
        Venta venta = producto.getVenta();
        if (venta == null) {
            return error(HttpStatus.NOT_FOUND, "No se encontró la factura de este producto.");
        }
        return ResponseEntity.ok(resumen(venta));
    }

    private static FacturaResumen resumen(Venta venta) {
        List<String> renglones = new ArrayList<>();
        for (DetalleVenta detalle : venta.getDetalles()) {
            renglones.add(detalle.getCantidad() + " × " + detalle.getArticulo().getNombre());
        }
        return new FacturaResumen(venta.getIdVenta(), venta.getFecha(), venta.getTotal(),
                String.join(", ", renglones));
    }

    // Descarga la factura simple en PDF de una venta ya registrada. El VENDEDOR solo puede
    // descargar sus propias ventas (mismo criterio que listarVentas); el ADMINISTRADOR y el
    // ATENCION (que la consulta desde "Nueva solicitud"), cualquiera. Con ?abrir=true el PDF se
    // muestra en el navegador (pestaña nueva) en vez de descargarse.
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR', 'ATENCION')")
    @GetMapping("/ventas/{id}/factura")
    public ResponseEntity<?> factura(@PathVariable Long id,
                                     @RequestParam(defaultValue = "false") boolean abrir,
                                     @AuthenticationPrincipal UsuarioPrincipal principal) {
        Venta venta = ventaRepo.findById(id).orElse(null);
        if (venta == null) {
            return error(HttpStatus.NOT_FOUND, "La venta no existe.");
        }

        Usuario usuario = principal.getUsuario();
        String rol = usuario.getRol().getNombre();
        boolean veTodas = "ADMINISTRADOR".equals(rol) || "ATENCION".equals(rol);
        boolean esPropia = venta.getVendedor() != null
                && venta.getVendedor().getIdUsuario().equals(usuario.getIdUsuario());
        if (!veTodas && !esPropia) {
            return error(HttpStatus.FORBIDDEN, "No tenés permiso para ver esta factura.");
        }

        byte[] pdf;
        try {
            pdf = facturaService.generar(venta);
        } catch (IOException e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar la factura.");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        (abrir ? "inline" : "attachment") + "; filename=\"factura-" + venta.getIdVenta() + ".pdf\"")
                .body(pdf);
    }

    // Genera un Producto por cada unidad vendida. Queda exactamente igual que los demás productos:
    // tipo = categoría del artículo, marca y modelo de fábrica, N° de serie en formato SN-XX-0000,
    // cliente, fecha de venta y la venta (factura) de la que salió.
    private void registrarProductosDeLaVenta(Venta venta, List<DetalleVenta> detalles) {
        for (DetalleVenta detalle : detalles) {
            Articulo articulo = detalle.getArticulo();

            for (int unidad = 1; unidad <= detalle.getCantidad(); unidad++) {
                Producto producto = new Producto();
                producto.setTipoProducto(articulo.getCategoria().trim());
                producto.setMarca(articulo.getMarca().trim());
                producto.setModelo(articulo.getModelo().trim());
                producto.setNroSerie(siguienteNroSerie(producto.getTipoProducto()));
                producto.setCliente(venta.getCliente());
                producto.setFechaVenta(LocalDate.now());
                producto.setVenta(venta);
                productoRepo.save(producto);
            }
        }
    }

    private static boolean datosDeProductoCompletos(Articulo articulo) {
        return tieneTexto(articulo.getCategoria()) && tieneTexto(articulo.getMarca()) && tieneTexto(articulo.getModelo());
    }

    // Próximo N° de serie SN-{prefijo}-{número}. El prefijo es el que ya usa ese tipo de producto
    // (AA para "Aire acondicionado", TV para "Televisor", ...); si el tipo es nuevo, son sus dos
    // primeras letras. El número sigue al mayor ya usado con ese prefijo, así nunca se repite.
    private String siguienteNroSerie(String tipoProducto) {
        String prefijo = null;
        for (String serie : productoRepo.findNroSerieDeTipo(tipoProducto)) {
            Matcher m = SERIE_ESTANDAR.matcher(serie);
            if (m.matches()) {
                prefijo = m.group(1);
                break;
            }
        }
        if (prefijo == null) {
            String letras = Normalizer.normalize(tipoProducto, Normalizer.Form.NFD)
                    .replaceAll("[^A-Za-z]", "").toUpperCase();
            prefijo = (letras + "XX").substring(0, 2);
        }

        int maximo = 0;
        for (String serie : productoRepo.findNroSerieConPrefijo("SN-" + prefijo + "-")) {
            Matcher m = SERIE_ESTANDAR.matcher(serie);
            if (m.matches()) {
                maximo = Math.max(maximo, Integer.parseInt(m.group(2)));
            }
        }
        return String.format("SN-%s-%04d", prefijo, maximo + 1);
    }

    private static boolean tieneTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    // Igual que error(), pero además deshace lo ya descontado del stock de los renglones anteriores
    // de esta venta (si no, un rechazo a mitad del carrito dejaba el stock descontado sin venta).
    private static ResponseEntity<Map<String, String>> errorConReversion(HttpStatus estado, String mensaje) {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        return error(estado, mensaje);
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }
}
