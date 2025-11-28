package com.example.SA2Gemini.entity;

import jakarta.persistence.*;

@Entity
public class TipoProveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    // Default constructor required by JPA
    public TipoProveedor() {
    }

    // Constructor for DataInitializer
    public TipoProveedor(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}