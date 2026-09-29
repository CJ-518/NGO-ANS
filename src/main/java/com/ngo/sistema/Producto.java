package com.ngo.sistema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Un producto vendido (equipo con número de serie, usado para el seguimiento de garantías).
 * Todo producto representa una venta concreta, por lo que siempre tiene el {@link Cliente}
 * que lo compró y la fecha en que se vendió.
 * La garantía se calcula siempre a partir de esa fecha (1 año) en vez de guardarse aparte,
 * así nunca queda desincronizada.
 */
@Entity
@Table(name = "producto")
public class Producto {

    // Clave primaria autogenerada.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    // Marca de fábrica (se copia del Articulo al momento de vender).
    @Column(nullable = false, length = 50)
    private String marca;

    // Modelo de fábrica (se copia del Articulo al momento de vender).
    @Column(nullable = false, length = 50)
    private String modelo;

    // N° de serie único de la unidad, con formato SN-XX-0000
    // (lo genera VentaController#siguienteNroSerie).
    @Column(name = "nro_serie", nullable = false, length = 50, unique = true)
    private String nroSerie;

    // Tipo de producto (Heladera, Televisor, ...): es la categoría del Articulo vendido.
    @Column(name = "tipo_producto", nullable = false, length = 50)
    private String tipoProducto;

    // Cliente que compró este producto. Obligatorio: un producto no puede existir sin dueño.
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    // Fecha en la que se vendió el producto. A partir de acá se calcula la garantía (1 año).
    @Column(name = "fecha_venta", nullable = false)
    private LocalDate fechaVenta;

    // Venta (factura) de la que salió este producto. Es la misma relación para los productos vendidos
    // desde el Portal de Ventas y para los del historial. No se serializa: no forma parte del JSON de productos.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta")
    private Venta venta;

    // Getters and Setters
    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getNroSerie() { return nroSerie; }
    public void setNroSerie(String nroSerie) { this.nroSerie = nroSerie; }

    public String getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDate getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDate fechaVenta) { this.fechaVenta = fechaVenta; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    // ---------- Garantía calculada (fecha de venta + 1 año) ----------

    /**
     * Fecha en que vence la garantía: fecha de venta + 1 año. No se guarda en la base (@Transient)
     * y tampoco se serializa (@JsonIgnore): el front la obtiene desde /api/productos/{id}/garantia.
     */
    @Transient
    @JsonIgnore
    public LocalDate getFechaFinGarantia() {
        return fechaVenta == null ? null : fechaVenta.plusYears(1);
    }

    /**
     * Indica si la garantía sigue vigente hoy: la fecha de fin no es anterior a la fecha actual.
     */
    @Transient
    @JsonIgnore
    public boolean isGarantiaVigente() {
        LocalDate fin = getFechaFinGarantia();
        return fin != null && !fin.isBefore(LocalDate.now());
    }
}
