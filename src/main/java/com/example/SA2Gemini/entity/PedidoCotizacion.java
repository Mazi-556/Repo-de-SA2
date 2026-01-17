package com.example.SA2Gemini.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList; // Importar ArrayList

@Entity
public class PedidoCotizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "solicitud_compra_id")
    private SolicitudCompra solicitudCompra; // La solicitud de compra original que originó este pedido

    @Enumerated(EnumType.STRING)
    private EstadoPedidoCotizacion estado; // ENVIADO, COTIZADO, RECHAZADO, FINALIZADO

    private boolean ordenCompraGenerada = false; // Indica si ya se generó una OC a partir de este pedido

    @OneToMany(mappedBy = "pedidoCotizacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PedidoCotizacionItem> items = new ArrayList<>();

    // Constructor por defecto
    public PedidoCotizacion() {
        this.fecha = LocalDate.now();
        this.estado = EstadoPedidoCotizacion.ENVIADO; // Estado inicial por defecto
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

    public SolicitudCompra getSolicitudCompra() {
        return solicitudCompra;
    }

    public void setSolicitudCompra(SolicitudCompra solicitudCompra) {
        this.solicitudCompra = solicitudCompra;
    }

    public EstadoPedidoCotizacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedidoCotizacion estado) {
        this.estado = estado;
    }

    public List<PedidoCotizacionItem> getItems() {
        return items;
    }

    public void setItems(List<PedidoCotizacionItem> items) {
        this.items = items;
        if (items != null) {
            for (PedidoCotizacionItem item : items) {
                item.setPedidoCotizacion(this);
            }
        }
    }

    public boolean isOrdenCompraGenerada() {
        return ordenCompraGenerada;
    }

    public void setOrdenCompraGenerada(boolean ordenCompraGenerada) {
        this.ordenCompraGenerada = ordenCompraGenerada;
    }
}
