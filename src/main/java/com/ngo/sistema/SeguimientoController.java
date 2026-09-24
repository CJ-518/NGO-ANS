package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Trabajo del técnico sobre las solicitudes que tiene asignadas: pasar a EN DIAGNÓSTICO
 * (con un texto opcional) o a FINALIZADA, dejando el registro en el seguimiento.
 * Solo puede hacerlo el TECNICO asignado a esa solicitud.
 */
@RestController
@RequestMapping("/api")
public class SeguimientoController {

    private static final int DIAGNOSTICO_MAX_CARACTERES = 2000;

    /** Cuerpo opcional al registrar el diagnóstico. */
    public record Avance(String diagnostico) {}

    /** Una entrada del historial de la solicitud. */
    public record RegistroSeguimiento(Long idSeguimiento, LocalDateTime fecha, String estado,
                                      String diagnostico, String usuario) {}

    @Autowired
    private SolicitudRepository solicitudRepo;

    @Autowired
    private AsignacionRepository asignacionRepo;

    @Autowired
    private SeguimientoRepository seguimientoRepo;

    // Historial de una solicitud (lo puede consultar cualquier usuario con sesión)
    @GetMapping("/solicitudes/{id}/seguimiento")
    public ResponseEntity<?> historial(@PathVariable Long id) {
        if (!solicitudRepo.existsById(id)) {
            return error(HttpStatus.NOT_FOUND, "La solicitud no existe.");
        }
        List<RegistroSeguimiento> registros = seguimientoRepo
                .findBySolicitudIdSolicitudOrderByIdSeguimientoAsc(id).stream()
                .map(s -> new RegistroSeguimiento(
                        s.getIdSeguimiento(),
                        s.getFecha(),
                        s.getEstado(),
                        s.getDiagnostico(),
                        s.getUsuario() != null ? s.getUsuario().getNombre() : null))
                .toList();
        return ResponseEntity.ok(registros);
    }

    @PreAuthorize("hasRole('TECNICO')")
    @PostMapping("/solicitudes/{id}/diagnostico")
    @Transactional
    public ResponseEntity<?> registrarDiagnostico(@PathVariable Long id,
                                                  @RequestBody(required = false) Avance datos,
                                                  @AuthenticationPrincipal UsuarioPrincipal principal) {
        String texto = null;
        if (datos != null && datos.diagnostico() != null) {
            texto = datos.diagnostico().trim();
            if (texto.isEmpty()) {
                texto = null; // el texto es opcional
            } else if (texto.length() > DIAGNOSTICO_MAX_CARACTERES) {
                return error(HttpStatus.BAD_REQUEST,
                        "El diagnóstico es demasiado largo (máximo " + DIAGNOSTICO_MAX_CARACTERES + " caracteres).");
            }
        }
        return cambiarEstado(id, "EN DIAGNÓSTICO", texto, principal);
    }

    @PreAuthorize("hasRole('TECNICO')")
    @PostMapping("/solicitudes/{id}/finalizar")
    @Transactional
    public ResponseEntity<?> finalizar(@PathVariable Long id,
                                       @AuthenticationPrincipal UsuarioPrincipal principal) {
        return cambiarEstado(id, "FINALIZADA", null, principal);
    }

    private ResponseEntity<?> cambiarEstado(Long id, String nuevoEstado, String diagnostico, UsuarioPrincipal principal) {
        Usuario tecnico = principal.getUsuario();

        Solicitud solicitud = solicitudRepo.findById(id).orElse(null);
        if (solicitud == null) {
            return error(HttpStatus.NOT_FOUND, "La solicitud no existe.");
        }

        // Solo el técnico que tiene asignada la solicitud (la última asignación) puede avanzarla
        Asignacion vigente = asignacionRepo.findFirstBySolicitudIdSolicitudOrderByIdAsignacionDesc(id).orElse(null);
        boolean esSuya = vigente != null
                && vigente.getUsuario() != null
                && vigente.getUsuario().getIdUsuario().equals(tecnico.getIdUsuario());
        if (!esSuya) {
            return error(HttpStatus.FORBIDDEN, "Solo el técnico asignado puede cambiar el estado de esta solicitud.");
        }

        if ("FINALIZADA".equals(solicitud.getEstadoActual())) {
            return error(HttpStatus.CONFLICT, "La solicitud ya está finalizada.");
        }

        // Queda el registro en el seguimiento y se actualiza el estado de la solicitud
        Seguimiento registro = new Seguimiento();
        registro.setSolicitud(solicitud);
        registro.setUsuario(tecnico);
        registro.setEstado(nuevoEstado);
        registro.setDiagnostico(diagnostico);
        seguimientoRepo.save(registro);

        solicitud.setEstadoActual(nuevoEstado);
        solicitudRepo.save(solicitud);

        return ResponseEntity.ok(solicitud);
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }
}
