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
    private RemitoRepository remitoRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private OrdenCompraRepository ordenCompraRepository;
    @Autowired
    private SolicitudCompraRepository solicitudCompraRepository;
    @Autowired
    private AsientoService asientoService; // El servicio existente
    @Autowired
    private CuentaRepository cuentaRepository; // Repositorio de Cuentas

    @Transactional
    public Factura crearFacturaYAsiento(Factura factura, BigDecimal subtotal, BigDecimal iva) throws Exception {
        // Stock is updated on Remito creation, so we no longer do it here.

        // 1. Preparar el Asiento Contable
        Asiento asiento = new Asiento();
        asiento.setFecha(LocalDate.now());
        asiento.setDescripcion("Asiento por Factura de Compra N°: " + factura.getNumeroFactura());

        List<Movimiento> movimientos = new ArrayList<>();
        
        // Asumimos que estas cuentas existen. En un caso real, se debería manejar si no se encuentran.
        Cuenta cuentaMercaderias = cuentaRepository.findByNombre("Mercaderías")
                .orElseThrow(() -> new Exception("La cuenta 'Mercaderías' no existe."));
        Cuenta cuentaIvaCredito = cuentaRepository.findByNombre("IVA Crédito Fiscal")
                .orElseThrow(() -> new Exception("La cuenta 'IVA Crédito Fiscal' no existe."));
        Cuenta cuentaProveedores = cuentaRepository.findByNombre("Proveedores")
                .orElseThrow(() -> new Exception("La cuenta 'Proveedores' no existe."));

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
        movHaberProveedores.setHaber(subtotal.add(iva));
        movimientos.add(movHaberProveedores);

        asiento.setMovimientos(movimientos);

        // 2. Llamar al AsientoService existente
        asientoService.saveAsiento(asiento);
        
        factura.setAsiento(asiento);

        // 3. Finalizar Estados
        OrdenCompra oc = factura.getOrdenCompra();
        // The OC status is now updated in RemitoService. We might only finalize it here.
        // For now, we assume the invoice means the process is complete.
        oc.setEstado(EstadoOrdenCompra.RECIBIDA_COMPLETA);
        ordenCompraRepository.save(oc);

        SolicitudCompra sc = oc.getSolicitudCompra();
        if (sc != null) {
            sc.setEstado(EstadoSolicitud.FINALIZADA);
            solicitudCompraRepository.save(sc);
        }
        
        return facturaRepository.save(factura);
    }
}
