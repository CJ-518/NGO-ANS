package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Asignación de técnicos a solicitudes. Pueden asignar el ADMINISTRADOR y el FUNCIONARIO
 * (SecurityConfig también protege POST /api/solicitudes/{id}/asignar).
 */
@RestController
@RequestMapping("/api")
public class AsignacionController {

    /** Cuerpo del pedido: el usuario (con rol TECNICO) que se asigna. */
    public record NuevaAsignacion(Long idTecnico) {}

    /** Técnico disponible para asignar. */
    public record TecnicoResumen(Long idUsuario, String nombre) {}

    @Autowired
    private SolicitudRepository solicitudRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private AsignacionRepository asignacionRepo;

    // Lista de técnicos activos, para el selector de la tabla
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FUNCIONARIO')")
    @GetMapping("/tecnicos")
    public List<TecnicoResumen> listarTecnicos() {
        return usuarioRepo.findByRolNombreAndEstadoOrderByNombreAsc("TECNICO", "ACTIVO").stream()
                .map(u -> new TecnicoResumen(u.getIdUsuario(), u.getNombre()))
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FUNCIONARIO')")
    @PostMapping("/solicitudes/{id}/asignar")
    public ResponseEntity<?> asignar(@PathVariable Long id, @RequestBody NuevaAsignacion datos) {
        if (datos.idTecnico() == null) {
            return error(HttpStatus.BAD_REQUEST, "Indicá el técnico que se va a asignar.");
        }

        Solicitud solicitud = solicitudRepo.findById(id).orElse(null);
        if (solicitud == null) {
            return error(HttpStatus.NOT_FOUND, "La solicitud no existe.");
        }

        // Solo se puede asignar a un usuario con rol TECNICO que esté ACTIVO
        Usuario tecnico = usuarioRepo.findById(datos.idTecnico()).orElse(null);
        boolean esTecnicoActivo = tecnico != null
                && tecnico.getRol() != null
                && "TECNICO".equals(tecnico.getRol().getNombre())
                && "ACTIVO".equals(tecnico.getEstado());
        if (!esTecnicoActivo) {
            return error(HttpStatus.BAD_REQUEST, "El usuario elegido no es un técnico activo.");
        }

        // Si ya es el técnico vigente, no se duplica la asignación
        Asignacion ultima = asignacionRepo.findFirstBySolicitudIdSolicitudOrderByIdAsignacionDesc(id).orElse(null);
        boolean yaAsignado = ultima != null
                && ultima.getUsuario() != null
                && ultima.getUsuario().getIdUsuario().equals(tecnico.getIdUsuario());
        if (!yaAsignado) {
            Asignacion nueva = new Asignacion();
            nueva.setSolicitud(solicitud);
            nueva.setUsuario(tecnico);
            asignacionRepo.save(nueva); // las asignaciones anteriores quedan como historial
        }

        // Al asignar técnico, una solicitud recién recibida pasa a ASIGNADA
        if ("RECIBIDA".equals(solicitud.getEstadoActual())) {
            solicitud.setEstadoActual("ASIGNADA");
            solicitudRepo.save(solicitud);
        }

        return ResponseEntity.ok(solicitud);
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }
}