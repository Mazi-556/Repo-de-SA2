package com.example.SA2Gemini.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList; // Importar ArrayList
import java.math.BigDecimal; // Importar BigDecimal

@Entity
public class OrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Enumerated(EnumType.STRING)
    private EstadoOrdenCompra estado; // Por ejemplo: EMITIDA, EN_PROCESO, RECIBIDA_PARCIAL, RECIBIDA_COMPLETA

    private String formaPago;
    private String plazoPago;
    private boolean conEnvio;

    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;

    @OneToOne
    @JoinColumn(name = "solicitud_compra_id")
    private SolicitudCompra solicitudCompra; // Referencia a la SolicitudCompra que originó esta OC

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrdenCompraItem> items;

    // Constructor por defecto
    public OrdenCompra() {
        this.fecha = LocalDate.now();
        this.estado = EstadoOrdenCompra.EMITIDA; // Estado inicial por defecto
        this.items = new ArrayList<>(); // Inicializar la lista de ítems
    }

    // Getters y Setters
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

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public EstadoOrdenCompra getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrdenCompra estado) {
        this.estado = estado;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public String getPlazoPago() {
        return plazoPago;
    }

    public void setPlazoPago(String plazoPago) {
        this.plazoPago = plazoPago;
    }

    public boolean isConEnvio() {
        return conEnvio;
    }

    public void setConEnvio(boolean conEnvio) {
        this.conEnvio = conEnvio;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public SolicitudCompra getSolicitudCompra() {
        return solicitudCompra;
    }

    public void setSolicitudCompra(SolicitudCompra solicitudCompra) {
        this.solicitudCompra = solicitudCompra;
    }

    public List<OrdenCompraItem> getItems() {
        return items;
    }

    // Método helper para añadir ítems y establecer la relación bidireccional
    public void addOrderItem(OrdenCompraItem item) {
        this.items.add(item);
        item.setOrdenCompra(this);
    }
}
