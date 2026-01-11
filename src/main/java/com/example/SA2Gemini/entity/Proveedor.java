package com.example.SA2Gemini.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import java.util.List;

@Entity
@EntityListeners({AuditingEntityListener.class, com.example.SA2Gemini.config.AuditEntityListener.class})
public class Proveedor extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String razonSocial;
    private String cuit;
    private String telefono;
    private String correo;
    private String provincia;
    private String ciudad;
    private String codigoPostal;
    private String direccion;
    private String banco;
    private String numeroCuenta;
    private boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "tipo_proveedor_id")
    private TipoProveedor tipoProveedor;

    @ManyToOne
    @JoinColumn(name = "rubro_id")
    private Rubro rubro;

    @OneToMany(mappedBy = "proveedor")
    private List<ProductoProveedor> productoProveedores;

    @CreatedBy
    private String creadoPor;

    @CreatedDate
    private LocalDateTime fechaCreacion;

    // Getters
    public String getCreadoPor() { return creadoPor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

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

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public TipoProveedor getTipoProveedor() {
        return tipoProveedor;
    }

    public void setTipoProveedor(TipoProveedor tipoProveedor) {
        this.tipoProveedor = tipoProveedor;
    }

    public Rubro getRubro() {
        return rubro;
    }

    public void setRubro(Rubro rubro) {
        this.rubro = rubro;
    }

    public List<ProductoProveedor> getProductoProveedores() {
        return productoProveedores;
    }

    public void setProductoProveedores(List<ProductoProveedor> productoProveedores) {
        this.productoProveedores = productoProveedores;
    }
}