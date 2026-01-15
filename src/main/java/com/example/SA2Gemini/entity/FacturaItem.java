package com.example.SA2Gemini.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class FacturaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orden_compra_item_id")
    private OrdenCompraItem ordenCompraItem;

    private int cantidad;

    // Precio unitario sin IVA (bruto)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    // Porcentaje de IVA aplicado a este ítem (ej 21)
    private BigDecimal ivaPorcentaje = BigDecimal.valueOf(21);

    // Montos calculados y persistidos
    private BigDecimal subtotal = BigDecimal.ZERO; // cantidad * precioUnitario
    private BigDecimal ivaMonto = BigDecimal.ZERO; // subtotal * ivaPorcentaje/100
    private BigDecimal total = BigDecimal.ZERO; // subtotal + ivaMonto

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public OrdenCompraItem getOrdenCompraItem() {
        return ordenCompraItem;
    }

    public void setOrdenCompraItem(OrdenCompraItem ordenCompraItem) {
        this.ordenCompraItem = ordenCompraItem;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getIvaPorcentaje() {
        return ivaPorcentaje;
    }

    public void setIvaPorcentaje(BigDecimal ivaPorcentaje) {
        this.ivaPorcentaje = ivaPorcentaje;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIvaMonto() {
        return ivaMonto;
    }

    public void setIvaMonto(BigDecimal ivaMonto) {
        this.ivaMonto = ivaMonto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
