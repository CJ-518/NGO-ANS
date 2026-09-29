package com.ngo.sistema;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Venta registrada desde el Portal de Ventas (equivale a una factura). Contiene sus renglones
 * ({@link DetalleVenta}) y, si se vendió a un cliente registrado, el {@link Cliente}.
 */
@Entity
@Table(name = "venta")
public class Venta {

    // Clave primaria autogenerada. Es el número de factura.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVenta;

    // Quién la registró (rol VENDEDOR o ADMINISTRADOR)
    @ManyToOne
    @JoinColumn(name = "id_vendedor", nullable = false)
    private Usuario vendedor;

    // Opcional: venta a un cliente ya registrado en el sistema
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    // Fecha y hora de la venta: la completa la base (DEFAULT CURRENT_TIMESTAMP).
    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha;

    // Total de la venta = suma de los subtotales de sus detalles.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    // Renglones de la venta. cascade = ALL: al guardar la venta se guardan sus detalles;
    // orphanRemoval: un detalle quitado de la lista se borra de la base.
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    // Getters and Setters
    public Long getIdVenta() { return idVenta; }
    public void setIdVenta(Long idVenta) { this.idVenta = idVenta; }

    public Usuario getVendedor() { return vendedor; }
    public void setVendedor(Usuario vendedor) { this.vendedor = vendedor; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
}
