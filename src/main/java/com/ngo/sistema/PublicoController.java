package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Consulta pública del estado de una solicitud, sin necesidad de usuario ni contraseña:
 * el cliente accede con el link que le pasa NGO SAECA (seguimiento.html?codigo=...), que
 * lleva el código aleatorio de la solicitud (no el id secuencial, para que no se pueda
 * adivinar el link de otro cliente probando números).
 *
 * Mientras la solicitud espera a un técnico (RECIBIDA o ASIGNADA) informa cuánto falta para que
 * la atiendan: el tiempo estimado se cuenta desde que se creó la solicitud y vence pasadas las
 * horas configuradas en ngo.atencion.plazo-horas. Si vence y nadie la atendió, el cliente puede
 * enviar un reclamo desde el mismo link (uno cada ngo.atencion.reclamo-intervalo-horas como mínimo).
 *
 * Solo se expone lo que le sirve al cliente: no se devuelven datos del cliente ni el nombre
 * del técnico o funcionario que atendió cada paso.
 */
@RestController
@RequestMapping("/api/publico")
public class PublicoController {

    // Estados en los que la solicitud todavía espera a un técnico: la atención empieza cuando
    // el técnico la pasa a EN DIAGNÓSTICO.
    private static final List<String> ESTADOS_EN_ESPERA = List.of("RECIBIDA", "ASIGNADA");

    private static final int RECLAMO_MAX_CARACTERES = 500;

    // Horas, desde que se crea la solicitud, que se estima que tarda un técnico en empezar a atenderla.
    @Value("${ngo.atencion.plazo-horas:48}")
    private long plazoHoras;

    // Horas que tienen que pasar entre un reclamo y el siguiente sobre la misma solicitud.
    @Value("${ngo.atencion.reclamo-intervalo-horas:12}")
    private long reclamoIntervaloHoras;

    @Autowired
    private SolicitudRepository solicitudRepo;

    @Autowired
    private SeguimientoRepository seguimientoRepo;

    @Autowired
    private ReclamoRepository reclamoRepo;

    /**
     * Un paso del historial visible para el cliente (sin el nombre de quien lo hizo).
     */
    public record PasoSeguimiento(LocalDateTime fecha, String estado, String diagnostico) {}

    /**
     * Respuesta pública con solo lo que le sirve al cliente: número de solicitud, estado,
     * descripción, datos del producto, historial y la espera de atención:
     * - esperandoAtencion: la solicitud todavía no la empezó a atender un técnico.
     * - fechaLimiteAtencion: hasta cuándo se estima que se la empieza a atender (null si no espera).
     * - segundosRestantes: lo que falta para esa fecha (0 si no espera o si ya venció).
     * - plazoVencido: la fecha estimada pasó y sigue sin atención.
     * - puedeReclamar: el cliente puede enviar un reclamo en este momento.
     * - ultimoReclamo: cuándo envió el último reclamo (null si no reclamó o si ya la atendieron).
     * - proximoReclamo: desde cuándo puede enviar otro (null si puede reclamar ya o no corresponde).
     */
    public record EstadoSolicitud(
            String numero,
            LocalDateTime fecha,
            String estadoActual,
            String descripcion,
            String productoMarca,
            String productoModelo,
            String productoTipo,
            List<PasoSeguimiento> historial,
            boolean esperandoAtencion,
            LocalDateTime fechaLimiteAtencion,
            long segundosRestantes,
            boolean plazoVencido,
            boolean puedeReclamar,
            LocalDateTime ultimoReclamo,
            LocalDateTime proximoReclamo) {}

    /** Cuerpo opcional del reclamo: el texto que el cliente quiera agregar. */
    public record NuevoReclamo(String mensaje) {}

    /**
     * GET /api/publico/seguimiento/{codigo}: devuelve el estado de una solicitud a partir de su código público.
     * No requiere sesión (ver SecurityConfig). Responde 404 si el código no existe.
     */
    @GetMapping("/seguimiento/{codigo}")
    public ResponseEntity<?> consultar(@PathVariable String codigo) {
        Solicitud solicitud = solicitudRepo.findByCodigoPublico(codigo).orElse(null);
        if (solicitud == null) {
            return error(HttpStatus.NOT_FOUND, "No se encontró ninguna solicitud con ese link.");
        }
        return ResponseEntity.ok(armarRespuesta(solicitud));
    }

    /**
     * POST /api/publico/seguimiento/{codigo}/reclamo: el cliente reclama porque pasó el tiempo estimado
     * y ningún técnico empezó a atender su solicitud (cuerpo opcional: {"mensaje": "..."}).
     * No requiere sesión (ver SecurityConfig). Responde con el estado actualizado de la solicitud, o:
     * 404 si el código no existe, 400 si el mensaje es demasiado largo, 409 si la solicitud ya fue
     * atendida o todavía está dentro del tiempo estimado, 429 si el último reclamo es muy reciente.
     */
    @PostMapping("/seguimiento/{codigo}/reclamo")
    public ResponseEntity<?> reclamar(@PathVariable String codigo,
                                      @RequestBody(required = false) NuevoReclamo datos) {
        Solicitud solicitud = solicitudRepo.findByCodigoPublico(codigo).orElse(null);
        if (solicitud == null) {
            return error(HttpStatus.NOT_FOUND, "No se encontró ninguna solicitud con ese link.");
        }

        String texto = null;
        if (datos != null && datos.mensaje() != null) {
            texto = datos.mensaje().trim();
            if (texto.isEmpty()) {
                texto = null; // el texto es opcional
            } else if (texto.length() > RECLAMO_MAX_CARACTERES) {
                return error(HttpStatus.BAD_REQUEST,
                        "El mensaje es demasiado largo (máximo " + RECLAMO_MAX_CARACTERES + " caracteres).");
            }
        }

        // Las reglas de cuándo se puede reclamar están en armarRespuesta: acá solo se aplican
        EstadoSolicitud estado = armarRespuesta(solicitud);
        if (!estado.esperandoAtencion()) {
            return error(HttpStatus.CONFLICT, "Tu solicitud ya fue atendida, por eso no hace falta enviar un reclamo.");
        }
        if (!estado.plazoVencido()) {
            return error(HttpStatus.CONFLICT, "Todavía estás dentro del tiempo estimado de atención.");
        }
        if (!estado.puedeReclamar()) {
            return error(HttpStatus.TOO_MANY_REQUESTS,
                    "Ya enviaste un reclamo hace poco. Esperá un tiempo antes de enviar otro.");
        }

        Reclamo reclamo = new Reclamo();
        reclamo.setSolicitud(solicitud);
        reclamo.setMensaje(texto);
        reclamoRepo.save(reclamo);

        return ResponseEntity.ok(armarRespuesta(solicitud));
    }

    /**
     * Arma lo que ve el cliente de una solicitud. La espera de atención solo corre mientras la solicitud
     * está en un estado de ESTADOS_EN_ESPERA: el tiempo estimado vence "plazoHoras" después de su creación
     * y, una vez vencido, el cliente puede reclamar si pasó el tiempo mínimo desde su último reclamo.
     */
    private EstadoSolicitud armarRespuesta(Solicitud solicitud) {
        List<PasoSeguimiento> historial = seguimientoRepo
                .findBySolicitudIdSolicitudOrderByIdSeguimientoAsc(solicitud.getIdSolicitud()).stream()
                .map(s -> new PasoSeguimiento(s.getFecha(), s.getEstado(), s.getDiagnostico()))
                .toList();

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = solicitud.getFecha() != null ? solicitud.getFecha().plusHours(plazoHoras) : null;
        boolean esperando = limite != null && ESTADOS_EN_ESPERA.contains(solicitud.getEstadoActual());
        boolean vencido = esperando && !ahora.isBefore(limite);
        long segundosRestantes = esperando && !vencido ? Duration.between(ahora, limite).getSeconds() : 0;

        Reclamo ultimo = reclamoRepo
                .findFirstBySolicitudIdSolicitudOrderByIdReclamoDesc(solicitud.getIdSolicitud()).orElse(null);
        LocalDateTime desde = ultimo != null ? ultimo.getFecha().plusHours(reclamoIntervaloHoras) : null;
        boolean puedeReclamar = vencido && (desde == null || !ahora.isBefore(desde));

        return new EstadoSolicitud(
                solicitud.getNumero(),
                solicitud.getFecha(),
                solicitud.getEstadoActual(),
                solicitud.getDescripcion(),
                solicitud.getProducto().getMarca(),
                solicitud.getProducto().getModelo(),
                solicitud.getProducto().getTipoProducto(),
                historial,
                esperando,
                esperando ? limite : null,
                segundosRestantes,
                vencido,
                puedeReclamar,
                esperando && ultimo != null ? ultimo.getFecha() : null,
                vencido && desde != null && ahora.isBefore(desde) ? desde : null);
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }
}
