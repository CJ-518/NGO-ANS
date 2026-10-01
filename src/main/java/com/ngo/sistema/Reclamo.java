package com.ngo.sistema;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Reclamo que el cliente envía desde el link público de seguimiento cuando su solicitud sigue
 * esperando a un técnico después del tiempo estimado de atención. Cada envío es una fila: el
 * personal los ve en el detalle de la solicitud y como aviso en la tabla del panel.
 */
@Entity
@Table(name = "reclamo")
public class Reclamo {

    // Clave primaria autogenerada.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReclamo;

    // Solicitud sobre la que se reclama (FK id_solicitud).
    @ManyToOne
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;

    // Fecha del reclamo: se completa justo antes del INSERT, así está disponible apenas se guarda
    // (se usa para respetar el tiempo mínimo entre un reclamo y el siguiente).
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    // Texto opcional que escribe el cliente (hasta 500 caracteres; lo valida el controlador).
    @Column(length = 500)
    private String mensaje;

    /**
     * Callback de JPA: se ejecuta justo antes del INSERT. Completa la fecha con el momento actual.
     */
    @PrePersist
    private void completarFecha() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public Long getIdReclamo() { return idReclamo; }
    public void setIdReclamo(Long idReclamo) { this.idReclamo = idReclamo; }

    public Solicitud getSolicitud() { return solicitud; }
    public void setSolicitud(Solicitud solicitud) { this.solicitud = solicitud; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
