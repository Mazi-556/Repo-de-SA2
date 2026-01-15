package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.*;
import com.example.SA2Gemini.repository.*;
import com.example.SA2Gemini.repository.FacturaItemRepository;
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

    @Autowired
    private FacturaItemRepository facturaItemRepository;

    @Transactional
    public Factura crearFacturaYAsiento(Factura factura,
                                       List<Long> ordenCompraItemIdsSeleccionados,
                                       java.util.Map<Long, BigDecimal> ivaPorcentajePorItem) throws Exception {

        OrdenCompra oc = factura.getOrdenCompra();

        // Filtrar ítems de OC por selección
        List<OrdenCompraItem> itemsSeleccionados = oc.getItems().stream()
                .filter(i -> ordenCompraItemIdsSeleccionados.contains(i.getId()))
                .toList();

        if (itemsSeleccionados.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un ítem para facturar.");
        }

        // Calcular subtotal e IVA total según IVA% por ítem
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal ivaTotal = BigDecimal.ZERO;

        List<FacturaItem> facturaItems = new ArrayList<>();

        for (OrdenCompraItem ocItem : itemsSeleccionados) {
            BigDecimal precioUnitario = ocItem.getPrecioUnitario() != null ? ocItem.getPrecioUnitario() : BigDecimal.ZERO;
            BigDecimal subItem = precioUnitario.multiply(BigDecimal.valueOf(ocItem.getCantidad()));

            BigDecimal ivaPct = ivaPorcentajePorItem.getOrDefault(ocItem.getId(), BigDecimal.valueOf(21));
            BigDecimal tasaIva = ivaPct.divide(new BigDecimal("100"));
            BigDecimal ivaItem = subItem.multiply(tasaIva);
            BigDecimal totalItem = subItem.add(ivaItem);

            subtotal = subtotal.add(subItem);
            ivaTotal = ivaTotal.add(ivaItem);

            FacturaItem fi = new FacturaItem();
            fi.setFactura(factura);
            fi.setOrdenCompraItem(ocItem);
            fi.setCantidad(ocItem.getCantidad());
            fi.setPrecioUnitario(precioUnitario);
            fi.setIvaPorcentaje(ivaPct);
            fi.setSubtotal(subItem);
            fi.setIvaMonto(ivaItem);
            fi.setTotal(totalItem);
            facturaItems.add(fi);
        }

        BigDecimal total = subtotal.add(ivaTotal);

        factura.setSubtotal(subtotal);
        factura.setIvaTotal(ivaTotal);
        factura.setTotal(total);
        factura.setItems(facturaItems);

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

        // Movimiento del Debe para Mercaderías (Subtotal sin IVA)
        Movimiento movDebeMercaderias = new Movimiento();
        movDebeMercaderias.setCuenta(cuentaMercaderias);
        movDebeMercaderias.setDebe(subtotal);
        movDebeMercaderias.setHaber(BigDecimal.ZERO);
        movimientos.add(movDebeMercaderias);

        // Movimiento del Debe para IVA
        Movimiento movDebeIva = new Movimiento();
        movDebeIva.setCuenta(cuentaIvaCredito);
        movDebeIva.setDebe(ivaTotal);
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

        // Cambiar estado a FACTURADA para que no aparezca más en el listado de pendientes
        oc.setEstado(EstadoOrdenCompra.FACTURADA);
        ordenCompraRepository.save(oc);

        oc.getItems().forEach(ocItem -> {

        // Buscamos solicitudes tanto presupuestadas como las que ya tienen orden (comprometidas)
        List<SolicitudCompra> solicitudesAsociadas = solicitudCompraRepository.findAll().stream()
            .filter(s -> s.getEstado() == EstadoSolicitud.COTIZANDO || 
                         s.getEstado() == EstadoSolicitud.COTIZADA || 
             s.getEstado() == EstadoSolicitud.COMPROMETIDA || 
             s.getEstado() == EstadoSolicitud.INGRESADA)
            .toList();

            
            for (SolicitudCompra sc : solicitudesAsociadas) {
                // Lógica simple: si la solicitud tiene el producto que estamos facturando, la cerramos
                // Filtrar items con producto null antes de comparar
                boolean tieneProducto = sc.getItems().stream()
                        .filter(item -> item.getProducto() != null)  // Filtrar items sin producto
                        .anyMatch(item -> ocItem.getProducto() != null && 
                                         item.getProducto().getId().equals(ocItem.getProducto().getId()));
                if (tieneProducto) {
                    sc.setEstado(EstadoSolicitud.FINALIZADA);
                    solicitudCompraRepository.save(sc);
                }
            }
        });
        Factura saved = facturaRepository.save(factura);
        // guardar items (por si el cascade no aplica por configuración)
        facturaItemRepository.saveAll(facturaItems);
        return saved;
    }
}
