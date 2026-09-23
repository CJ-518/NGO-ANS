package com.ngo.sistema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Administración de usuarios: solo ADMINISTRADOR puede listar usuarios y dar de alta
 * funcionarios y técnicos. (SecurityConfig también protege /api/usuarios/**.)
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    // Roles que se pueden dar de alta desde el sistema
    private static final Set<String> ROLES_PERMITIDOS = Set.of("FUNCIONARIO", "TECNICO", "VENDEDOR");

    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int CLAVE_MIN_CARACTERES = 8;
    private static final int CLAVE_MAX_BYTES = 72; // límite de BCrypt

    /** Datos que llegan desde el formulario (la clave nunca se devuelve). */
    public record NuevoUsuario(String nombre, String correo, String clave, String rol) {}

    /** Datos que se devuelven de un usuario: sin la clave ni su hash. */
    public record UsuarioResumen(Long idUsuario, String nombre, String correo, String rol, String estado) {}

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private RolRepository rolRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<UsuarioResumen> listar() {
        return usuarioRepo.findAll(Sort.by("nombre")).stream()
                .map(UsuarioController::resumir)
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody NuevoUsuario datos) {
        String nombre = datos.nombre() == null ? "" : datos.nombre().trim();
        String correo = datos.correo() == null ? "" : datos.correo().trim();
        String clave = datos.clave() == null ? "" : datos.clave();
        String nombreRol = datos.rol() == null ? "" : datos.rol().trim().toUpperCase();

        if (nombre.isEmpty() || nombre.length() > 100) {
            return error(HttpStatus.BAD_REQUEST, "El nombre es obligatorio (máximo 100 caracteres).");
        }
        if (correo.length() > 100 || !CORREO.matcher(correo).matches()) {
            return error(HttpStatus.BAD_REQUEST, "El correo no es válido.");
        }
        if (clave.length() < CLAVE_MIN_CARACTERES) {
            return error(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos " + CLAVE_MIN_CARACTERES + " caracteres.");
        }
        if (clave.getBytes(StandardCharsets.UTF_8).length > CLAVE_MAX_BYTES) {
            return error(HttpStatus.BAD_REQUEST, "La contraseña es demasiado larga.");
        }
        if (!ROLES_PERMITIDOS.contains(nombreRol)) {
            return error(HttpStatus.BAD_REQUEST, "El rol debe ser FUNCIONARIO, TECNICO o VENDEDOR.");
        }
        if (usuarioRepo.existsByCorreoIgnoreCase(correo)) {
            return error(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.");
        }

        Rol rol = rolRepo.findByNombre(nombreRol).orElse(null);
        if (rol == null) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "El rol " + nombreRol + " no existe en la base de datos.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setClave(passwordEncoder.encode(clave)); // se guarda cifrada con BCrypt
        usuario.setRol(rol);
        usuario.setEstado("ACTIVO");

        try {
            usuario = usuarioRepo.save(usuario);
        } catch (DataIntegrityViolationException e) {
            // Dos altas simultáneas con el mismo correo: la restricción UNIQUE de la base lo detecta
            return error(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(resumir(usuario));
    }

    private static UsuarioResumen resumir(Usuario u) {
        return new UsuarioResumen(
                u.getIdUsuario(),
                u.getNombre(),
                u.getCorreo(),
                u.getRol() != null ? u.getRol().getNombre() : null,
                u.getEstado());
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }
}