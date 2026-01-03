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
    public Venta registrarVenta(Long productoId, Integer cantidad, String formaPago) { // 1. Cambiamos la firma
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar que haya stock suficiente
        if (producto.getStockActual() < cantidad) {
            throw new RuntimeException("Stock insuficiente. Stock actual: " + producto.getStockActual());
        }

        // Crear la venta
        Venta venta = new Venta();
        venta.setProducto(producto);
        venta.setCantidad(cantidad);
        venta.setPrecioUnitario(producto.getPrecioVenta() != null ? producto.getPrecioVenta() : BigDecimal.ZERO);
        venta.setTotal(venta.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad)));
        venta.setFecha(LocalDate.now());
        venta.setFormaPago(formaPago); // 2. Seteamos la forma de pago

        // Disminuir el stock
        producto.setStockActual(producto.getStockActual() - cantidad);
        productoRepository.save(producto);
        
        // TRIGGER DE PUNTO DE REPOSICIÓN 
        if (producto.getStockActual() < producto.getPuntoReposicion()) {
            solicitudCompraService.crearSolicitudAutomatica(producto);
        }

        try {
            Asiento asiento = new Asiento();
            asiento.setFecha(LocalDate.now());
            asiento.setDescripcion("Venta de producto: " + producto.getNombre());
            asiento.setMovimientos(new ArrayList<>());

            // Lógica de cuentas según forma de pago
            String nombreCuentaDebe = switch (formaPago) {
                case "EFECTIVO" -> "Caja";
                case "TRANSFERENCIA" -> "Banco";
                case "CHEQUE" -> "Valores a depositar";
                default -> "Caja";
            };

            Cuenta cuentaDebe = cuentaRepository.findByNombre(nombreCuentaDebe)
                    .orElseThrow(() -> new RuntimeException("No existe la cuenta: " + nombreCuentaDebe));

            Cuenta cuentaHaber = cuentaRepository.findByNombre("Ventas")
                    .orElseThrow(() -> new RuntimeException("No existe la cuenta: Ventas"));

            // Movimiento DEBE
            Movimiento movDebe = new Movimiento();
            movDebe.setCuenta(cuentaDebe);
            movDebe.setDebe(venta.getTotal());
            movDebe.setHaber(BigDecimal.ZERO);
            movDebe.setAsiento(asiento);
            asiento.getMovimientos().add(movDebe);

            // Movimiento HABER
            Movimiento movHaber = new Movimiento();
            movHaber.setCuenta(cuentaHaber);
            movHaber.setDebe(BigDecimal.ZERO);
            movHaber.setHaber(venta.getTotal());
            movHaber.setAsiento(asiento);
            asiento.getMovimientos().add(movHaber);

            asientoService.saveAsiento(asiento);

        } catch (Exception e) {
            // Si el asiento falla, se cancela todo (Rollback)
            throw new RuntimeException("Error contable: " + e.getMessage());
        }

        return ventaRepository.save(venta);
    }
    
    public List<Venta> getAllVentas() {
        return ventaRepository.findAllByOrderByFechaDesc();
    }
}
