package com.ngo.sistema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Un producto vendido (equipo con número de serie, usado para el seguimiento de garantías).
 * Ya no existe el "producto sin cliente": todo producto representa una venta concreta,
 * por lo que siempre tiene el {@link Cliente} que lo compró y la fecha en que se vendió.
 * La garantía se calcula siempre a partir de esa fecha (1 año) en vez de guardarse aparte,
 * así nunca queda desincronizada.
 */
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(name = "nro_serie", nullable = false, length = 50, unique = true)
    private String nroSerie;

    @Column(name = "tipo_producto", nullable = false, length = 50)
    private String tipoProducto;

    // Cliente que compró este producto. Obligatorio: un producto ya no puede existir sin dueño.
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    // Fecha en la que se vendió el producto. A partir de acá se calcula la garantía (1 año).
    @Column(name = "fecha_venta", nullable = false)
    private LocalDate fechaVenta;

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

    // ---------- Garantía calculada (fecha de venta + 1 año) ----------

    @Transient
    @JsonIgnore
    public LocalDate getFechaFinGarantia() {
        return fechaVenta == null ? null : fechaVenta.plusYears(1);
    }

    @Transient
    @JsonIgnore
    public boolean isGarantiaVigente() {
        LocalDate fin = getFechaFinGarantia();
        return fin != null && !fin.isBefore(LocalDate.now());
    }
}
