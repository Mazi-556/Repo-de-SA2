package com.example.SA2Gemini.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permiso")
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo; // Ej: "CUENTAS", "ASIENTOS", "VENTAS"

    @Column(nullable = false)
    private String nombre; // Ej: "Plan de Cuentas", "Registrar Asiento"

    @Column(nullable = false)
    private String urlPattern; // Ej: "/cuentas/**", "/asientos/**"

    private String descripcion;

    private String categoria; // Para agrupar en la UI: "Contabilidad", "Abastecimiento", etc.

    @ManyToMany(mappedBy = "permisos")
    private Set<Rol> roles = new HashSet<>();

    public Permiso() {
    }

    public Permiso(String codigo, String nombre, String urlPattern, String descripcion, String categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.urlPattern = urlPattern;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permiso permiso = (Permiso) o;
        return codigo != null && codigo.equals(permiso.codigo);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
