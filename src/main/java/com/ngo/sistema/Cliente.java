package com.ngo.sistema;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

/**
 * Cliente de la empresa: quien compra productos y abre solicitudes de servicio técnico.
 * El documento es único en la base de datos y sirve como clave de búsqueda.
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    // Clave primaria autogenerada.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    // Nombre y apellido del cliente (obligatorio).
    @Column(nullable = false, length = 100)
    private String nombre;

    // Documento de identidad: solo dígitos. Se valida acá con @Pattern y también con un CHECK en la base.
    @Column(nullable = false, length = 20)
    @Pattern(regexp = "^[0-9]+$", message = "El documento debe contener solo números")
    private String documento;

    // Teléfono: solo dígitos con un "+" opcional al inicio (ej. +595981123456).
    @Column(nullable = false, length = 20)
    @Pattern(regexp = "^\\+?[0-9]+$", message = "El teléfono solo puede contener números y un signo + opcional al inicio")
    private String telefono;

    // Correo electrónico (opcional).
    @Column(length = 100)
    private String correo;

    // Getters and Setters
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
