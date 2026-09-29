package com.ngo.sistema;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registro del historial de una solicitud: cada vez que el técnico pasa la solicitud a
 * EN DIAGNÓSTICO o FINALIZADA se guarda una fila con el estado, quién lo hizo y un
 * diagnóstico opcional. Es lo que se muestra en "Seguimiento" (detalle y página pública).
 */
@Entity
@Table(name = "seguimiento")
public class Seguimiento {

    // Clave primaria autogenerada.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSeguimiento;

    // Solicitud a la que pertenece el registro (FK id_solicitud).
    @ManyToOne
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;

    // Usuario (técnico) que hizo el cambio (FK id_usuario).
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // Fecha del registro: la completa la base (DEFAULT CURRENT_TIMESTAMP).
    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha;

    // Estado al que pasó la solicitud (EN DIAGNÓSTICO o FINALIZADA).
    @Column(nullable = false, length = 30)
    private String estado;

    // Texto del diagnóstico del técnico (opcional, hasta 2000 caracteres; lo valida el controlador).
    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    public Long getIdSeguimiento() { return idSeguimiento; }
    public void setIdSeguimiento(Long idSeguimiento) { this.idSeguimiento = idSeguimiento; }

    public Solicitud getSolicitud() { return solicitud; }
    public void setSolicitud(Solicitud solicitud) { this.solicitud = solicitud; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
}
