package com.example.SA2Gemini.entity;

import jakarta.persistence.*;
// import lombok.Data; // Removed

// @Data // Removed
@Entity
public class CategoriaProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

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