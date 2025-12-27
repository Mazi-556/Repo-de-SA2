package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.*;
import com.example.SA2Gemini.repository.OrdenCompraRepository;
import com.example.SA2Gemini.repository.ProductoRepository;
import com.example.SA2Gemini.repository.RemitoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RemitoService {

    @Autowired
    private RemitoRepository remitoRepository;

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // Dentro de la clase OrdenCompraItem.java
    private int cantidadRecibida = 0; // Inicializar en 0 para que no sea nulo

        public int getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void setCantidadRecibida(int cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
    }

    public List<Remito> getRemitosByOrdenCompraId(Long ordenCompraId) {
        return remitoRepository.findByOrdenCompraId(ordenCompraId);
    }

    public java.util.Optional<Remito> getRemitoById(Long id) {
        return remitoRepository.findById(id);
    }
	
	@Transactional
    public List<OrdenCompraItem> getPendingItems(OrdenCompra ordenCompra) {
        List<Remito> remitosAsociados = remitoRepository.findAllByOrdenCompra(ordenCompra);

        return ordenCompra.getItems().stream().map(ocItem -> {
            int cantidadPedida = ocItem.getCantidad();
            int cantidadYaRecibida = remitosAsociados.stream()
                    .flatMap(r -> r.getItems().stream())
                    .filter(ri -> ri.getProducto().equals(ocItem.getProducto()))
                    .mapToInt(RemitoItem::getCantidad)
                    .sum();
            
            int cantidadPendiente = cantidadPedida - cantidadYaRecibida;
            ocItem.setCantidadPendiente(cantidadPendiente);
            
            return ocItem;
        }).filter(ocItem -> ocItem.getCantidadPendiente() > 0).collect(Collectors.toList());
    }

        @Transactional
        public Remito createRemito(Long ocId, LocalDate fechaRemito, Map<Long, Integer> itemQuantities) {

            
            OrdenCompra ordenCompra = ordenCompraRepository.findById(ocId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid orden de compra Id:" + ocId));
    
            Remito remito = new Remito();
            remito.setOrdenCompra(ordenCompra);
            remito.setFecha(fechaRemito);
    
            // Fetch all remitos for the given OC once
            List<Remito> allRemitosForOC = remitoRepository.findAllByOrdenCompra(ordenCompra);
    
            Map<Long, OrdenCompraItem> ocItemsMap = ordenCompra.getItems().stream()
                    .collect(Collectors.toMap(OrdenCompraItem::getId, Function.identity()));
    
            for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
                Long itemId = entry.getKey();
                Integer cantidadRecibida = entry.getValue();
    
                if (cantidadRecibida != null && cantidadRecibida > 0) {
                    OrdenCompraItem ocItem = ocItemsMap.get(itemId);
                    if (ocItem == null) {
                        throw new IllegalArgumentException("Invalid item Id:" + itemId);
                    }
    
                    RemitoItem remitoItem = new RemitoItem();
                    remitoItem.setRemito(remito);
                    remitoItem.setProducto(ocItem.getProducto());
                    remitoItem.setCantidad(cantidadRecibida);
                    remito.getItems().add(remitoItem); // Maintain bidirectional relationship
    
                    Producto producto = ocItem.getProducto();
                    producto.setStockActual(producto.getStockActual() + cantidadRecibida);
                    productoRepository.save(producto);
                }
            }
    
            Remito savedRemito = remitoRepository.save(remito);
            allRemitosForOC.add(savedRemito); // Add current remito to the list for calculation
    
            // Update OC Status
            boolean allItemsCompleted = ordenCompra.getItems().stream().allMatch(ocItem -> {
                int cantidadPedida = ocItem.getCantidad();
                int cantidadYaRecibida = allRemitosForOC.stream()
                        .flatMap(r -> r.getItems().stream())
                        .filter(ri -> ri.getProducto().equals(ocItem.getProducto()))
                        .mapToInt(RemitoItem::getCantidad)
                        .sum();
                return cantidadYaRecibida >= cantidadPedida;
            });
    
                    if (allItemsCompleted) {
                        ordenCompra.setEstado(EstadoOrdenCompra.RECIBIDA_COMPLETA);
                    } else {                long itemsReceivedCount = ordenCompra.getItems().stream().filter(ocItem ->
                    allRemitosForOC.stream()
                        .flatMap(r -> r.getItems().stream())
                        .anyMatch(ri -> ri.getProducto().equals(ocItem.getProducto()))
                ).count();
    
                if(itemsReceivedCount > 0) {
                    ordenCompra.setEstado(EstadoOrdenCompra.RECIBIDA_PARCIAL);
                }
            }
    
            ordenCompraRepository.save(ordenCompra);
    
            return savedRemito;
        }}
