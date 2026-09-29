package com.ngo.sistema;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Artículo del catálogo de ventas: es independiente de {@link Producto}
 * (que representa un equipo con número de serie para garantías). Acá cada
 * fila es un tipo de artículo con una cantidad disponible en stock.
 */
@Entity
@Table(name = "articulo")
public class Articulo {

    // Clave primaria autogenerada (columna IDENTITY de PostgreSQL).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idArticulo;

    // Nombre comercial: se muestra en el catálogo, en el carrito y en la factura (obligatorio).
    @Column(nullable = false, length = 100)
    private String nombre;

    // Descripción libre. El portal de ventas la muestra como tooltip sobre el nombre.
    @Column(length = 255)
    private String descripcion;

    // Tipo de producto (Heladera, Televisor, ...): es el tipo_producto de cada unidad vendida
    @Column(length = 50)
    private String categoria;

    // Marca y modelo de fábrica: se copian a cada Producto (unidad con N° de serie) que sale de una venta
    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    // Precio unitario en guaraníes. Se usa BigDecimal (no double) para no perder precisión en dinero.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    // Unidades disponibles. Se descuenta dentro de la transacción de la venta
    // (VentaController#registrarVenta) y nunca puede ser negativo (CHECK en la base).
    @Column(nullable = false)
    private Integer stock = 0;

    // Estado del artículo: solo los ACTIVO aparecen en el catálogo (ver ArticuloRepository).
    @Column(length = 30)
    private String estado = "ACTIVO";

    // Getters and Setters
    public Long getIdArticulo() { return idArticulo; }
    public void setIdArticulo(Long idArticulo) { this.idArticulo = idArticulo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
