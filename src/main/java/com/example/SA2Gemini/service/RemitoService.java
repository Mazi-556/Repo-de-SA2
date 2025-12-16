package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.*;
import com.example.SA2Gemini.repository.OrdenCompraItemRepository;
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
    private OrdenCompraItemRepository ordenCompraItemRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Remito> getRemitosByOrdenCompraId(Long ordenCompraId) {
        return remitoRepository.findByOrdenCompraId(ordenCompraId);
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
    public Remito createRemito(Long ocId, LocalDate fechaRemito, List<Long> itemIds, List<Integer> cantidades) {
        OrdenCompra ordenCompra = ordenCompraRepository.findById(ocId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid orden de compra Id:" + ocId));

        Remito remito = new Remito();
        remito.setOrdenCompra(ordenCompra);
        remito.setFecha(fechaRemito);

        Remito savedRemito = remitoRepository.save(remito);
        
        Map<Long, OrdenCompraItem> ocItemsMap = ordenCompra.getItems().stream()
                .collect(Collectors.toMap(OrdenCompraItem::getId, Function.identity()));

        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            Integer cantidadRecibida = cantidades.get(i);

            if (cantidadRecibida != null && cantidadRecibida > 0) {
                OrdenCompraItem ocItem = ocItemsMap.get(itemId);
                if (ocItem == null) {
                    throw new IllegalArgumentException("Invalid item Id:" + itemId);
                }

                // Crear el item del remito
                RemitoItem remitoItem = new RemitoItem();
                remitoItem.setRemito(savedRemito);
                remitoItem.setProducto(ocItem.getProducto());
                remitoItem.setCantidad(cantidadRecibida);
                
                // Actualizar stock del producto
                Producto producto = ocItem.getProducto();
                producto.setStockActual(producto.getStockActual() + cantidadRecibida);
                productoRepository.save(producto);
            }
        }
        
        // Lógica para actualizar estado de la OC
        long itemsCompletos = ordenCompra.getItems().stream().filter(ocItem -> {
            int cantidadPedida = ocItem.getCantidad();
            int cantidadYaRecibida = remitoRepository.findAllByOrdenCompra(ordenCompra).stream()
                .flatMap(r -> r.getItems().stream())
                .filter(ri -> ri.getProducto().equals(ocItem.getProducto()))
                .mapToInt(RemitoItem::getCantidad)
                .sum();
            return cantidadYaRecibida >= cantidadPedida;
        }).count();

        if (itemsCompletos == ordenCompra.getItems().size()) {
            ordenCompra.setEstado(EstadoOrdenCompra.RECIBIDA_COMPLETA);
        } else if (itemsCompletos > 0) {
            ordenCompra.setEstado(EstadoOrdenCompra.RECIBIDA_PARCIAL);
        }

        ordenCompraRepository.save(ordenCompra);

        return savedRemito;
    }
}
