package com.ngo.sistema;

import jakarta.persistence.*;

/**
 * Rol de un usuario. Roles del sistema: ADMINISTRADOR, ATENCION, TECNICO y VENDEDOR.
 * Spring Security los expone como autoridades ROLE_<nombre> (ver {@link UsuarioPrincipal}).
 */
@Entity
@Table(name = "rol")
public class Rol {

    // Clave primaria autogenerada.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRol;

    // Nombre del rol, en mayúsculas y sin tildes (es lo que comparan los controladores).
    @Column(nullable = false, length = 50)
    private String nombre;

    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
