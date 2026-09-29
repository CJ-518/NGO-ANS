package com.ngo.sistema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Renglón de una {@link Venta}: un artículo, su cantidad, el precio al momento de vender y el subtotal.
 */
@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {

    // Clave primaria autogenerada.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalle;

    // No se serializa: ya viaja anidado dentro de la Venta y evita la recursión
    // venta -> detalles -> venta -> ...
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;

    // Artículo vendido (FK id_articulo).
    @ManyToOne
    @JoinColumn(name = "id_articulo", nullable = false)
    private Articulo articulo;

    // Cantidad de unidades vendidas (siempre mayor a 0: CHECK en la base).
    @Column(nullable = false)
    private Integer cantidad;

    // Precio del artículo al momento de la venta (no cambia si después se actualiza el precio del catálogo)
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // Subtotal del renglón = precioUnitario × cantidad.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    // Getters and Setters
    public Long getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Long idDetalle) { this.idDetalle = idDetalle; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
