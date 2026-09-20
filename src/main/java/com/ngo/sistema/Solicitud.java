package com.ngo.sistema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSolicitud;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "id_garantia")
    private Garantia garantia;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "estado_actual", nullable = false, length = 30)
    private String estadoActual = "RECIBIDA";

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
    
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Garantia getGarantia() { return garantia; }
    public void setGarantia(Garantia garantia) { this.garantia = garantia; }
    
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }

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