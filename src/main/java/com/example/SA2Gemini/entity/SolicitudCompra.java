package com.example.SA2Gemini.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

//@Data // Removed
@Entity
public class SolicitudCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private String observaciones;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;
    private String proveedorSugerido; // Nuevo campo

    @OneToMany(mappedBy = "solicitudCompra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SolicitudCompraItem> items;

    // Default constructor
    public SolicitudCompra() {
        this.fecha = LocalDate.now();
        this.estado = EstadoSolicitud.INICIO;
        this.items = new java.util.ArrayList<>(); // Initialize the items list
    }

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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public String getProveedorSugerido() {
        return proveedorSugerido;
    }

    public void setProveedorSugerido(String proveedorSugerido) {
        this.proveedorSugerido = proveedorSugerido;
    }

    public List<SolicitudCompraItem> getItems() {
        return items;
    }

    public void setItems(List<SolicitudCompraItem> items) {
        this.items = items;
    }
}