package com.ngo.sistema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Solicitud de servicio técnico sobre un producto de un cliente.
 * Ciclo de estados (estadoActual): RECIBIDA → ASIGNADA → EN DIAGNÓSTICO → FINALIZADA.
 * Las asignaciones y el seguimiento cuelgan de esta entidad y se borran en cascada al eliminarla.
 */
@Entity
@Table(name = "solicitud")
public class Solicitud {

    // Clave primaria autogenerada (clave interna). En pantalla se muestra el campo "numero".
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSolicitud;

    // Número de solicitud visible para las personas: fecha de creación (ddMMyyyy) + guion + orden de
    // la solicitud dentro de ese día, con al menos 2 dígitos. Ej.: 28092026-01, 28092026-02, 29092026-01.
    // Lo genera SistemaController#crearSolicitud (nunca el cliente) y no cambia una vez creada.
    // El id numérico es la clave interna que usan las rutas /api/solicitudes/{id}.
    // La restricción UNIQUE y el NOT NULL están definidos en la base (db/ngo_ans.sql).
    @Column(name = "numero_solicitud", length = 20, updatable = false)
    private String numero;

    // Cliente que pidió el servicio (FK id_cliente).
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    // Producto (equipo con N° de serie) que se lleva a servicio (FK id_producto).
    // De su fecha de venta se calcula si está en garantía.
    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    // Fecha de creación: la completa la base (DEFAULT CURRENT_TIMESTAMP).
    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha;

    // Problema reportado por el cliente (obligatorio).
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    // Estado vigente de la solicitud (por defecto RECIBIDA). Lo cambian la asignación de
    // un técnico, el técnico asignado (diagnóstico/finalizar) o ADMINISTRADOR/ATENCION a mano.
    @Column(name = "estado_actual", nullable = false, length = 30)
    private String estadoActual = "RECIBIDA";

    // Código para que el cliente consulte el estado de su solicitud sin necesidad de una
    // cuenta: se genera solo al crear la solicitud (no es el id secuencial, para que no se
    // pueda adivinar el link de otro cliente probando números). Va en la URL pública
    // /seguimiento.html?codigo=... y en el endpoint público /api/publico/seguimiento/{codigo}.
    @Column(name = "codigo_publico", unique = true, length = 40, updatable = false)
    private String codigoPublico;

    /**
     * Callback de JPA: se ejecuta justo antes del INSERT. Genera el código público
     * (UUID sin guiones) si todavía no tiene uno.
     */
    @PrePersist
    private void generarCodigoPublico() {
        if (codigoPublico == null || codigoPublico.isBlank()) {
            codigoPublico = java.util.UUID.randomUUID().toString().replace("-", "");
        }
    }

    // Al eliminar una solicitud se eliminan también sus asignaciones y su seguimiento
    // (las FK de la base de datos no tienen ON DELETE CASCADE). No se exponen en el JSON.
    @JsonIgnore
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.REMOVE)
    private List<Asignacion> asignaciones = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.REMOVE)
    private List<Seguimiento> seguimientos = new ArrayList<>();

    // Getters and Setters
    public Long getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Long idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }

    /**
     * Solo tiene getter: el código se genera en {@link #generarCodigoPublico()} y nunca cambia
     * (updatable = false), porque forma parte del link que recibió el cliente.
     */
    public String getCodigoPublico() { return codigoPublico; }

    // Técnico asignado actualmente (la última asignación). Solo se lee: se envía al frontend
    // como "tecnicoAsignado": { idUsuario, nombre }, o null si todavía no tiene técnico.
    @JsonProperty(value = "tecnicoAsignado", access = JsonProperty.Access.READ_ONLY)
    public Map<String, Object> getTecnicoAsignado() {
        return asignaciones.stream()
                .filter(a -> a.getUsuario() != null)
                .max(Comparator.comparing(Asignacion::getIdAsignacion))
                .map(a -> Map.<String, Object>of(
                        "idUsuario", a.getUsuario().getIdUsuario(),
                        "nombre", a.getUsuario().getNombre()))
                .orElse(null);
    }
}
