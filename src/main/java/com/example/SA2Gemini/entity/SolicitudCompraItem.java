package com.example.SA2Gemini.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

//@Data // Removed
@Entity
public class SolicitudCompraItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitud_compra_id")
    private SolicitudCompra solicitudCompra;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private int cantidad;
    private BigDecimal precioUnitario; // Nuevo campo
    private String descripcion; // Autocompletado desde producto
    
    private boolean procesadoEnCotizacion = false; // Indica si ya fue incluido en un pedido de cotización

    // Default constructor
    public SolicitudCompraItem() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SolicitudCompra getSolicitudCompra() {
        return solicitudCompra;
    }

    public void setSolicitudCompra(SolicitudCompra solicitudCompra) {
        this.solicitudCompra = solicitudCompra;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public boolean isProcesadoEnCotizacion() {
        return procesadoEnCotizacion;
    }

    public void setProcesadoEnCotizacion(boolean procesadoEnCotizacion) {
        this.procesadoEnCotizacion = procesadoEnCotizacion;
    }
}