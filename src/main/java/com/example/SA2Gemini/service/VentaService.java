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
            // Creamos UN SOLO asiento para todo el movimiento
            Asiento asiento = new Asiento();
            asiento.setFecha(LocalDate.now());
            asiento.setDescripcion("Venta y CMV - Producto: " + producto.getNombre());
            asiento.setMovimientos(new ArrayList<>());

            // --- PARTE 1: El Ingreso (DEBE) ---
            // Si es CHEQUE, el sistema busca "Valores a depositar"
            String nombreCuentaDebe = switch (formaPago) {
                case "EFECTIVO" -> "Caja";
                case "TRANSFERENCIA" -> "Banco";
                case "CHEQUE" -> "Valores a depositar";
                default -> "Caja";
            };

            Cuenta cDebe = cuentaRepository.findByNombre(nombreCuentaDebe)
                    .orElseThrow(() -> new RuntimeException("No existe la cuenta: " + nombreCuentaDebe));
            
            // Agregamos el movimiento del dinero que entra
            agregarMovimiento(asiento, cDebe, venta.getTotal(), BigDecimal.ZERO);

            // --- PARTE 2: La Venta (HABER) ---
            Cuenta cVentas = cuentaRepository.findByNombre("Ventas")
                    .orElseThrow(() -> new RuntimeException("No existe la cuenta: Ventas"));
            
            // Registramos el ingreso por venta
            agregarMovimiento(asiento, cVentas, BigDecimal.ZERO, venta.getTotal());

            // --- PARTE 3: El Costo y la Mercadería (CMV a Mercaderías) ---
            BigDecimal precioCosto = producto.getPrecioCosto() != null ? producto.getPrecioCosto() : BigDecimal.ZERO;
            BigDecimal costoTotal = precioCosto.multiply(BigDecimal.valueOf(cantidad));

            if (costoTotal.compareTo(BigDecimal.ZERO) > 0) {
                Cuenta cCmv = cuentaRepository.findByNombre("Costo de Mercaderías Vendidas")
                        .orElseThrow(() -> new RuntimeException("No existe la cuenta: Costo de Mercaderías Vendidas"));
                Cuenta cMercaderias = cuentaRepository.findByNombre("Mercaderías")
                        .orElseThrow(() -> new RuntimeException("No existe la cuenta: Mercaderías"));

                // CMV (DEBE)
                agregarMovimiento(asiento, cCmv, costoTotal, BigDecimal.ZERO);
                // Mercaderías (HABER - Baja el stock contable)
                agregarMovimiento(asiento, cMercaderias, BigDecimal.ZERO, costoTotal);
            }

            // Guardamos el asiento único
            asientoService.saveAsiento(asiento);

        } catch (Exception e) {
            // Si algo falla, se cancela la venta (Rollback)
            throw new RuntimeException("Error al generar el asiento: " + e.getMessage());
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
