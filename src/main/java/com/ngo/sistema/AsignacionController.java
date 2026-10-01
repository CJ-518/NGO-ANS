package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Asignación de técnicos a solicitudes, manual (una solicitud) o automática (todas las que no tienen
 * técnico). Pueden asignar el ADMINISTRADOR y el ATENCION (SecurityConfig también protege
 * POST /api/solicitudes/{id}/asignar y POST /api/solicitudes/autoasignar).
 */
@RestController
@RequestMapping("/api")
public class AsignacionController {

    /** Cuerpo del pedido: el usuario (con rol TECNICO) que se asigna. */
    public record NuevaAsignacion(Long idTecnico) {}

    /** Técnico disponible para asignar, con la cantidad de solicitudes pendientes que tiene. */
    public record TecnicoResumen(Long idUsuario, String nombre, long pendientes) {}

    /** Una solicitud asignada por la autoasignación: su número y el técnico que recibió. */
    public record AsignacionHecha(String numero, String tecnico) {}

    /** Resultado de la autoasignación: cuántas solicitudes se asignaron y a quién. */
    public record ResultadoAutoasignacion(int asignadas, List<AsignacionHecha> detalle) {}

    // Evita que dos autoasignaciones simultáneas (dos personas apretando el botón a la vez)
    // lean la misma lista de solicitudes sin asignar y las asignen dos veces.
    private static final Object BLOQUEO_AUTOASIGNACION = new Object();

    @Autowired
    private SolicitudRepository solicitudRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private AsignacionRepository asignacionRepo;

    // Lista de técnicos activos, para el selector de la tabla. Vienen ordenados por carga de
    // trabajo: primero los que tienen menos solicitudes pendientes (a igual carga, por nombre).
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENCION')")
    @GetMapping("/tecnicos")
    public List<TecnicoResumen> listarTecnicos() {
        Map<Long, Long> pendientes = new HashMap<>();
        for (Object[] fila : asignacionRepo.contarPendientesPorTecnico()) {
            pendientes.put(((Number) fila[0]).longValue(), ((Number) fila[1]).longValue());
        }

        return usuarioRepo.findByRolNombreAndEstadoOrderByNombreAsc("TECNICO", "ACTIVO").stream()
                .map(u -> new TecnicoResumen(u.getIdUsuario(), u.getNombre(),
                        pendientes.getOrDefault(u.getIdUsuario(), 0L)))
                .sorted(Comparator.comparingLong(TecnicoResumen::pendientes)
                        .thenComparing(TecnicoResumen::nombre))
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENCION')")
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

        // El estado refleja la asignación: al asignar (o reasignar) un técnico la solicitud queda
        // en ASIGNADA. Una solicitud ya FINALIZADA no se reabre.
        String estado = solicitud.getEstadoActual();
        boolean finalizada = "FINALIZADA".equals(estado);
        if (!finalizada && (!yaAsignado || "RECIBIDA".equals(estado))) {
            solicitud.setEstadoActual("ASIGNADA");
            solicitudRepo.save(solicitud);
        }

        return ResponseEntity.ok(solicitud);
    }

    /**
     * Autoasigna, de a una, todas las solicitudes que no tienen técnico. Por cada solicitud (de la más
     * antigua a la más nueva): consulta cuántas pendientes tiene cada técnico activo en ese momento,
     * elige al que tiene menos (a igual cantidad, el primero por nombre), le asigna ESA solicitud y
     * recién entonces pasa a la siguiente, volviendo a consultar las cantidades ya actualizadas.
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENCION')")
    @PostMapping("/solicitudes/autoasignar")
    public ResponseEntity<?> autoasignar() {
        synchronized (BLOQUEO_AUTOASIGNACION) {
            List<Usuario> tecnicos = usuarioRepo.findByRolNombreAndEstadoOrderByNombreAsc("TECNICO", "ACTIVO");
            if (tecnicos.isEmpty()) {
                return error(HttpStatus.BAD_REQUEST, "No hay técnicos activos para asignar solicitudes.");
            }

            List<AsignacionHecha> hechas = new ArrayList<>();
            for (Solicitud solicitud : solicitudRepo.findSinAsignar()) {
                // Cantidades al día: incluyen las solicitudes que se acaban de asignar en este mismo proceso
                Usuario elegido = tecnicoConMenosPendientes(tecnicos);

                Asignacion nueva = new Asignacion();
                nueva.setSolicitud(solicitud);
                nueva.setUsuario(elegido);
                asignacionRepo.save(nueva);

                solicitud.setEstadoActual("ASIGNADA");
                solicitudRepo.save(solicitud);

                hechas.add(new AsignacionHecha(solicitud.getNumero(), elegido.getNombre()));
            }
            return ResponseEntity.ok(new ResultadoAutoasignacion(hechas.size(), hechas));
        }
    }

    // Técnico con menos solicitudes pendientes en este momento. "tecnicos" ya viene ordenada por nombre,
    // y min() se queda con el primero ante un empate, así que a igual carga gana el de nombre menor.
    private Usuario tecnicoConMenosPendientes(List<Usuario> tecnicos) {
        Map<Long, Long> pendientes = new HashMap<>();
        for (Object[] fila : asignacionRepo.contarPendientesPorTecnico()) {
            pendientes.put(((Number) fila[0]).longValue(), ((Number) fila[1]).longValue());
        }
        return tecnicos.stream()
                .min(Comparator.comparingLong((Usuario u) -> pendientes.getOrDefault(u.getIdUsuario(), 0L)))
                .orElseThrow();
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }
}
