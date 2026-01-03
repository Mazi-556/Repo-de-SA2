package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.Asiento;
import com.example.SA2Gemini.entity.Cuenta;
import com.example.SA2Gemini.entity.Movimiento;
import com.example.SA2Gemini.entity.Producto;
import com.example.SA2Gemini.entity.Venta;
import com.example.SA2Gemini.repository.CuentaRepository;
import com.example.SA2Gemini.repository.ProductoRepository;
import com.example.SA2Gemini.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SolicitudCompraService solicitudCompraService;
    
    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private AsientoService asientoService;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Transactional
    public void simularVenta(Long productoId, int cantidadVendida) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStockActual() < cantidadVendida) {
            throw new RuntimeException("Stock insuficiente");
        }

        producto.setStockActual(producto.getStockActual() - cantidadVendida);
        productoRepository.save(producto);

        // TRIGGER DE PUNTO DE REPOSICIÓN
        if (producto.getStockActual() < producto.getPuntoReposicion()) {
            solicitudCompraService.crearSolicitudAutomatica(producto);
        }
    }
    
    @Transactional
    public Venta registrarVenta(Long productoId, Integer cantidad, String formaPago) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStockActual() < cantidad) {
            throw new RuntimeException("Stock insuficiente. Stock actual: " + producto.getStockActual());
        }

        // 1. Crear el objeto Venta
        Venta venta = new Venta();
        venta.setProducto(producto);
        venta.setCantidad(cantidad);
        venta.setPrecioUnitario(producto.getPrecioVenta() != null ? producto.getPrecioVenta() : BigDecimal.ZERO);
        venta.setTotal(venta.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad)));
        venta.setFecha(LocalDate.now());
        venta.setFormaPago(formaPago);

        // 2. Actualizar stock físico
        producto.setStockActual(producto.getStockActual() - cantidad);
        productoRepository.save(producto);
        
        if (producto.getStockActual() < producto.getPuntoReposicion()) {
            solicitudCompraService.crearSolicitudAutomatica(producto);
        }

        try {
            // --- ASIENTO 1: REGISTRO DE VENTA (Precio de Venta) ---
            Asiento asientoVenta = new Asiento();
            asientoVenta.setFecha(LocalDate.now());
            asientoVenta.setDescripcion("Venta de producto: " + producto.getNombre());
            asientoVenta.setMovimientos(new ArrayList<>());

            String nombreCuentaDebe = switch (formaPago) {
                case "EFECTIVO" -> "Caja";
                case "TRANSFERENCIA" -> "Banco";
                case "CHEQUE" -> "Valores a depositar";
                default -> "Caja";
            };

            Cuenta cDebe = cuentaRepository.findByNombre(nombreCuentaDebe)
                    .orElseThrow(() -> new RuntimeException("No existe: " + nombreCuentaDebe));
            Cuenta cVentas = cuentaRepository.findByNombre("Ventas")
                    .orElseThrow(() -> new RuntimeException("No existe: Ventas"));

            // Caja/Banco a Ventas
            agregarMovimiento(asientoVenta, cDebe, venta.getTotal(), BigDecimal.ZERO);
            agregarMovimiento(asientoVenta, cVentas, BigDecimal.ZERO, venta.getTotal());
            asientoService.saveAsiento(asientoVenta);

            // --- ASIENTO 2: REGISTRO DE COSTO (Precio de Costo) ---
            BigDecimal precioCosto = producto.getPrecioCosto() != null ? producto.getPrecioCosto() : BigDecimal.ZERO;
            BigDecimal costoTotal = precioCosto.multiply(BigDecimal.valueOf(cantidad));

            if (costoTotal.compareTo(BigDecimal.ZERO) > 0) {
                Asiento asientoCosto = new Asiento();
                asientoCosto.setFecha(LocalDate.now());
                asientoCosto.setDescripcion("CMV por venta de: " + producto.getNombre());
                asientoCosto.setMovimientos(new ArrayList<>());

                Cuenta cCmv = cuentaRepository.findByNombre("Costo de Mercaderías Vendidas")
                        .orElseThrow(() -> new RuntimeException("No existe: Costo de Mercaderías Vendidas"));
                Cuenta cMercaderias = cuentaRepository.findByNombre("Mercaderías")
                        .orElseThrow(() -> new RuntimeException("No existe: Mercaderías"));

                // CMV a Mercaderías
                agregarMovimiento(asientoCosto, cCmv, costoTotal, BigDecimal.ZERO);
                agregarMovimiento(asientoCosto, cMercaderias, BigDecimal.ZERO, costoTotal);
                asientoService.saveAsiento(asientoCosto);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error contable: " + e.getMessage());
        }

        return ventaRepository.save(venta);
    }

    // Método auxiliar para limpiar el código de movimientos
    private void agregarMovimiento(Asiento a, Cuenta c, BigDecimal debe, BigDecimal haber) {
        Movimiento m = new Movimiento();
        m.setCuenta(c);
        m.setDebe(debe);
        m.setHaber(haber);
        m.setAsiento(a);
        a.getMovimientos().add(m);
    }
    
    public List<Venta> getAllVentas() {
        return ventaRepository.findAllByOrderByFechaDesc();
    }
}
