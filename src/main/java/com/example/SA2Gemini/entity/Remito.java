package com.example.SA2Gemini.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

//@Data // Removed
@Entity
public class Remito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "orden_compra_id")
    private OrdenCompra ordenCompra;

    @OneToMany(mappedBy = "remito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RemitoItem> items;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public OrdenCompra getOrdenCompra() {
        return ordenCompra;
    }

    public void setOrdenCompra(OrdenCompra ordenCompra) {
        this.ordenCompra = ordenCompra;
    }

    public List<RemitoItem> getItems() {
        return items;
    }

    public void setItems(List<RemitoItem> items) {
        this.items = items;
    }
}