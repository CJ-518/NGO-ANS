package com.ngo.sistema;

import jakarta.persistence.*;

@Entity
@Table(name = "servicio_autorizado")
public class ServicioAutorizado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idServicio;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 30)
    private String estado = "ACTIVO";

    public Long getIdServicio() { return idServicio; }
    public void setIdServicio(Long idServicio) { this.idServicio = idServicio; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}