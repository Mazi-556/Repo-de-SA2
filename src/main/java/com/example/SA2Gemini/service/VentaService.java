package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.Producto;
import com.example.SA2Gemini.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SolicitudCompraService solicitudCompraService;

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
}
