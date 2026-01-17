package com.example.SA2Gemini.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column
    private TipoCuenta tipoCuenta;

    private boolean activo = true;
    
    // Indica si la cuenta puede tener saldo negativo (ej: Banco c/c puede tener descubierto)
    private boolean permiteSaldoNegativo = false;

    @Column
    private Long cuentaPadreId;

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

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

    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(TipoCuenta tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public Long getCuentaPadreId() {
        return cuentaPadreId;
    }

    public void setCuentaPadreId(Long cuentaPadreId) {
        this.cuentaPadreId = cuentaPadreId;
    }

    public boolean isPermiteSaldoNegativo() {
        return permiteSaldoNegativo;
    }

    public void setPermiteSaldoNegativo(boolean permiteSaldoNegativo) {
        this.permiteSaldoNegativo = permiteSaldoNegativo;
    }
}
