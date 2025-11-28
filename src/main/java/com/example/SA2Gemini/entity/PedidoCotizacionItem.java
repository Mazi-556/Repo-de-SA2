package com.example.SA2Gemini.entity;

import jakarta.persistence.*;
import java.math.BigDecimal; // Importar BigDecimal

@Entity
public class PedidoCotizacionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pedido_cotizacion_id")
    private PedidoCotizacion pedidoCotizacion;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private int cantidad;
    private BigDecimal precioUnitarioCotizado; // Precio cotizado por el proveedor
    private BigDecimal totalItemCotizado; // cantidad * precioUnitarioCotizado

    // Constructor por defecto
    public PedidoCotizacionItem() {}

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PedidoCotizacion getPedidoCotizacion() {
        return pedidoCotizacion;
    }

    public void setPedidoCotizacion(PedidoCotizacion pedidoCotizacion) {
        this.pedidoCotizacion = pedidoCotizacion;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitarioCotizado() {
        return precioUnitarioCotizado;
    }

    public void setPrecioUnitarioCotizado(BigDecimal precioUnitarioCotizado) {
        this.precioUnitarioCotizado = precioUnitarioCotizado;
    }

    public BigDecimal getTotalItemCotizado() {
        return totalItemCotizado;
    }

    public void setTotalItemCotizado(BigDecimal totalItemCotizado) {
        this.totalItemCotizado = totalItemCotizado;
    }
}
