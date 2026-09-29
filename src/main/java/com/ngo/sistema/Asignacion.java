package com.ngo.sistema;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Asignación de un técnico a una solicitud. Cada (re)asignación es una fila nueva: la ÚLTIMA
 * (mayor idAsignacion) es la vigente y las anteriores quedan como historial.
 */
@Entity
@Table(name = "asignacion")
public class Asignacion {

    // Clave primaria autogenerada.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAsignacion;

    // Solicitud a la que se asigna el técnico (FK id_solicitud).
    @ManyToOne
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;

    // Usuario con rol TECNICO que atiende la solicitud (FK id_usuario).
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // La fecha la completa la base (DEFAULT CURRENT_TIMESTAMP), por eso JPA no la inserta ni la actualiza.
    @Column(name = "fecha_asignacion", insertable = false, updatable = false)
    private LocalDateTime fechaAsignacion;

    public Long getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(Long idAsignacion) { this.idAsignacion = idAsignacion; }

    public Solicitud getSolicitud() { return solicitud; }
    public void setSolicitud(Solicitud solicitud) { this.solicitud = solicitud; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
}
