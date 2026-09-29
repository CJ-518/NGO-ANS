package com.ngo.sistema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Usuario del sistema (personal interno: administrador, atención, técnico o vendedor).
 * El correo es el nombre de usuario para iniciar sesión.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    // Clave primaria autogenerada.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    // Rol del usuario (FK id_rol). Define qué puede hacer en el sistema.
    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    // Nombre y apellido que se muestra en el encabezado y en los selectores de técnicos.
    @Column(nullable = false, length = 100)
    private String nombre;

    // Correo electrónico: es único y actúa como nombre de usuario en el login.
    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    // Nunca debe viajar al frontend: si en el futuro se serializa un Usuario completo
    // (por ejemplo, anidado dentro de una Venta como "vendedor"), el hash no se expone.
    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String clave;

    // ACTIVO o INACTIVO. Solo un usuario ACTIVO puede iniciar sesión (ver UsuarioPrincipal#isEnabled).
    @Column(length = 30)
    private String estado = "ACTIVO";

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
