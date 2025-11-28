package com.example.SA2Gemini.entity;

import jakarta.persistence.*;

//@Data // Removed
@Entity
public class RemitoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "remito_id")
    private Remito remito;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private int cantidad; // Cantidad recibida (puede ser parcial)

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Remito getRemito() {
        return remito;
    }

    public void setRemito(Remito remito) {
        this.remito = remito;
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
}