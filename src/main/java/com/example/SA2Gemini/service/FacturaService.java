package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.*;
import com.example.SA2Gemini.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;
    @Autowired
    private OrdenCompraRepository ordenCompraRepository;
    @Autowired
    private SolicitudCompraRepository solicitudCompraRepository;
    @Autowired
    private AsientoService asientoService; // El servicio existente
    @Autowired
    private CuentaRepository cuentaRepository; // Repositorio de Cuentas

    @Transactional
    public Factura crearFacturaYAsiento(Factura factura, BigDecimal ivaPorcentaje) throws Exception {

        OrdenCompra oc = factura.getOrdenCompra();

        // Calcular subtotal e IVA de la orden de compra
        BigDecimal subtotal = oc.getItems().stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        // Convertimos el porcentaje (ej: 21) en un decimal (ej: 0.21) para multiplicar
        BigDecimal tasaIva = ivaPorcentaje.divide(new BigDecimal("100"));
        BigDecimal iva = subtotal.multiply(tasaIva);
        BigDecimal total = subtotal.add(iva);

        factura.setTotal(total);

        Asiento asiento = new Asiento();
        asiento.setFecha(LocalDate.now());
        asiento.setDescripcion("Asiento por Factura de Compra N°: " + factura.getNumeroFactura());

        List<Movimiento> movimientos = new ArrayList<>();
        
        // Buscar cuentas por código (más robusto que buscar por nombre)
        Cuenta cuentaMercaderias = cuentaRepository.findByCodigo("1.2.1")
                .orElseThrow(() -> new Exception("La cuenta 1.2.1 (Mercaderías) no existe."));
        Cuenta cuentaIvaCredito = cuentaRepository.findByCodigo("1.2.2")
                .orElseThrow(() -> new Exception("La cuenta 1.2.2 (IVA Crédito Fiscal) no existe."));
        Cuenta cuentaProveedores = cuentaRepository.findByCodigo("2.1.1")
                .orElseThrow(() -> new Exception("La cuenta 2.1.1 (Proveedores) no existe."));

        // Movimiento del Debe para Mercaderías
        Movimiento movDebeMercaderias = new Movimiento();
        movDebeMercaderias.setCuenta(cuentaMercaderias);
        movDebeMercaderias.setDebe(subtotal);
        movDebeMercaderias.setHaber(BigDecimal.ZERO);
        movimientos.add(movDebeMercaderias);

        // Movimiento del Debe para IVA
        Movimiento movDebeIva = new Movimiento();
        movDebeIva.setCuenta(cuentaIvaCredito);
        movDebeIva.setDebe(iva);
        movDebeIva.setHaber(BigDecimal.ZERO);
        movimientos.add(movDebeIva);

        // Movimiento del Haber para Proveedores
        Movimiento movHaberProveedores = new Movimiento();
        movHaberProveedores.setCuenta(cuentaProveedores);
        movHaberProveedores.setDebe(BigDecimal.ZERO);
        movHaberProveedores.setHaber(total);
        movimientos.add(movHaberProveedores);

        asiento.setMovimientos(movimientos);

        // 2. Llamar al AsientoService existente
        asientoService.saveAsiento(asiento);
        
        factura.setAsiento(asiento);

        oc.setEstado(EstadoOrdenCompra.RECIBIDA_COMPLETA);
        ordenCompraRepository.save(oc);

        oc.getItems().forEach(ocItem -> {

        // Buscamos solicitudes tanto presupuestadas como las que ya tienen orden (comprometidas)
        List<SolicitudCompra> solicitudesAsociadas = solicitudCompraRepository.findAll().stream()
            .filter(s -> s.getEstado() == EstadoSolicitud.PRESUPUESTADA || s.getEstado() == EstadoSolicitud.COMPROMETIDA)
            .toList();
                       
            for (SolicitudCompra sc : solicitudesAsociadas) {
                // Lógica simple: si la solicitud tiene el producto que estamos facturando, la cerramos
                boolean tieneProducto = sc.getItems().stream()
                        .anyMatch(item -> item.getProducto().getId().equals(ocItem.getProducto().getId()));
                if (tieneProducto) {
                    sc.setEstado(EstadoSolicitud.FINALIZADA);
                    solicitudCompraRepository.save(sc);
                }
            }
        });
        return facturaRepository.save(factura);
    }
}
