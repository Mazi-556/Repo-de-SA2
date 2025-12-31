package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.Producto;
import com.example.SA2Gemini.entity.Venta;
import com.example.SA2Gemini.repository.ProductoRepository;
import com.example.SA2Gemini.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SolicitudCompraService solicitudCompraService;
    
    @Autowired
    private VentaRepository ventaRepository;

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
    public Venta registrarVenta(Long productoId, Integer cantidad) {
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

        // Disminuir el stock
        producto.setStockActual(producto.getStockActual() - cantidad);
        productoRepository.save(producto);
        
        // TRIGGER DE PUNTO DE REPOSICIÓN
        if (producto.getStockActual() < producto.getPuntoReposicion()) {
            solicitudCompraService.crearSolicitudAutomatica(producto);
        }

        return ventaRepository.save(venta);
    }
    
    public List<Venta> getAllVentas() {
        return ventaRepository.findAllByOrderByFechaDesc();
    }
}
