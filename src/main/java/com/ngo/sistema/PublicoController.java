package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Consulta pública del estado de una solicitud, sin necesidad de usuario ni contraseña:
 * el cliente accede con el link que le pasa NGO SAECA (seguimiento.html?codigo=...), que
 * lleva el código aleatorio de la solicitud (no el id secuencial, para que no se pueda
 * adivinar el link de otro cliente probando números).
 *
 * Solo se expone lo que le sirve al cliente: no se devuelven datos del cliente ni el nombre
 * del técnico o funcionario que atendió cada paso.
 */
@RestController
@RequestMapping("/api/publico")
public class PublicoController {

    @Autowired
    private SolicitudRepository solicitudRepo;

    @Autowired
    private SeguimientoRepository seguimientoRepo;

    public record PasoSeguimiento(LocalDateTime fecha, String estado, String diagnostico) {}

    public record EstadoSolicitud(
            Long idSolicitud,
            LocalDateTime fecha,
            String estadoActual,
            String descripcion,
            String productoMarca,
            String productoModelo,
            String productoTipo,
            List<PasoSeguimiento> historial) {}

    @GetMapping("/seguimiento/{codigo}")
    public ResponseEntity<?> consultar(@PathVariable String codigo) {
        Solicitud solicitud = solicitudRepo.findByCodigoPublico(codigo).orElse(null);
        if (solicitud == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No se encontró ninguna solicitud con ese link."));
        }

        List<PasoSeguimiento> historial = seguimientoRepo
                .findBySolicitudIdSolicitudOrderByIdSeguimientoAsc(solicitud.getIdSolicitud()).stream()
                .map(s -> new PasoSeguimiento(s.getFecha(), s.getEstado(), s.getDiagnostico()))
                .toList();

        EstadoSolicitud respuesta = new EstadoSolicitud(
                solicitud.getIdSolicitud(),
                solicitud.getFecha(),
                solicitud.getEstadoActual(),
                solicitud.getDescripcion(),
                solicitud.getProducto().getMarca(),
                solicitud.getProducto().getModelo(),
                solicitud.getProducto().getTipoProducto(),
                historial);

        return ResponseEntity.ok(respuesta);
    }
}
