package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.PedidoCotizacion;
import com.example.SA2Gemini.entity.PedidoCotizacionItem;
import com.example.SA2Gemini.entity.Proveedor; // Importar Proveedor
import com.example.SA2Gemini.entity.SolicitudCompra;
import com.example.SA2Gemini.entity.SolicitudCompraItem; // Importar SolicitudCompraItem
import com.example.SA2Gemini.entity.EstadoPedidoCotizacion; // Importar EstadoPedidoCotizacion
import com.example.SA2Gemini.entity.EstadoSolicitud;
import com.example.SA2Gemini.repository.PedidoCotizacionRepository;
import com.example.SA2Gemini.repository.ProveedorRepository;
import com.example.SA2Gemini.repository.SolicitudCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SA2Gemini.repository.SolicitudCompraItemRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.Hibernate; // Importar Hibernate

@Service
public class PedidoCotizacionService {

    @Autowired
    private PedidoCotizacionRepository pedidoCotizacionRepository;
    @Autowired
    private ProveedorRepository proveedorRepository;
    @Autowired
    private SolicitudCompraService solicitudCompraService; // Para obtener los SolicitudCompraItem
    @Autowired
    private SolicitudCompraRepository solicitudCompraRepository;
    @Autowired
    private SolicitudCompraItemRepository solicitudCompraItemRepository;

    @Transactional
    public PedidoCotizacion guardarPedidoCotizacion(
            Long proveedorId,
            Map<Long, Integer> itemCantidades) {

        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + proveedorId));

        PedidoCotizacion pedidoCotizacion = new PedidoCotizacion();
        pedidoCotizacion.setProveedor(proveedor);
        pedidoCotizacion.setFecha(LocalDate.now());
        // El estado por defecto ya es ENVIADO por el constructor

        List<Long> solicitudCompraItemIds = new ArrayList<>(itemCantidades.keySet());
        List<SolicitudCompraItem> selectedSolicitudItems = solicitudCompraService.getSolicitudCompraItemsByIds(solicitudCompraItemIds);

        // Asignar la SolicitudCompra al PedidoCotizacion (todos los items pertenecen a la misma SC)
        if (!selectedSolicitudItems.isEmpty()) {
            SolicitudCompra solicitudCompra = selectedSolicitudItems.get(0).getSolicitudCompra();
            pedidoCotizacion.setSolicitudCompra(solicitudCompra);
        }

        for (SolicitudCompraItem scItem : selectedSolicitudItems) {
            // Verificar si el ítem corresponde al proveedor para el que se está generando la cotización
            boolean isProvierForProduct = scItem.getProducto().getProductoProveedores().stream()
                                            .anyMatch(pp -> pp.getProveedor().getId().equals(proveedorId));
            
            if (isProvierForProduct) {
                PedidoCotizacionItem pcItem = new PedidoCotizacionItem();
                pcItem.setProducto(scItem.getProducto());
                pcItem.setCantidad(itemCantidades.getOrDefault(scItem.getId(), scItem.getCantidad()));
                pcItem.setPedidoCotizacion(pedidoCotizacion); // Establecer la relación bidireccional
                pedidoCotizacion.getItems().add(pcItem);
                
                // Marcar el item de solicitud como procesado
                scItem.setProcesadoEnCotizacion(true);
                solicitudCompraItemRepository.save(scItem);
            }
        }
        
        PedidoCotizacion savedPedido = pedidoCotizacionRepository.save(pedidoCotizacion);
        
        // Verificar si todos los items de la solicitud han sido procesados
        // Si es así, cambiar el estado de la solicitud a COTIZANDO
        if (savedPedido.getSolicitudCompra() != null) {
            SolicitudCompra solicitud = savedPedido.getSolicitudCompra();
            boolean todosItemsProcesados = solicitud.getItems().stream()
                    .allMatch(SolicitudCompraItem::isProcesadoEnCotizacion);
            
            if (todosItemsProcesados && solicitud.getEstado() == EstadoSolicitud.PENDIENTE) {
                solicitud.setEstado(EstadoSolicitud.COTIZANDO);
                solicitudCompraRepository.save(solicitud);
            }
        }
        
        return savedPedido;
    }

    @Transactional
    public PedidoCotizacion cargarCotizacionProveedor(
            Long pedidoCotizacionId,
            Map<Long, BigDecimal> preciosCotizados,
            Map<Long, Integer> cantidadesFinales) {

        PedidoCotizacion pedido = pedidoCotizacionRepository.findById(pedidoCotizacionId)
                .orElseThrow(() -> new RuntimeException("Pedido de Cotización no encontrado con ID: " + pedidoCotizacionId));

        System.out.println("DEBUG PedidoCotizacionService - Antes de actualizar ítems: " + pedido.getItems().size() + " ítems.");

        for (PedidoCotizacionItem item : pedido.getItems()) {
            Long itemId = item.getId();
            if (preciosCotizados.containsKey(itemId)) {
                item.setPrecioUnitarioCotizado(preciosCotizados.get(itemId));
                item.setCantidad(cantidadesFinales.getOrDefault(itemId, item.getCantidad()));
                item.setTotalItemCotizado(item.getPrecioUnitarioCotizado().multiply(new BigDecimal(item.getCantidad())));
                System.out.println("DEBUG PC Item Actualizado: ID=" + itemId + ", Precio=" + item.getPrecioUnitarioCotizado() + ", Cantidad=" + item.getCantidad() + ", Total=" + item.getTotalItemCotizado());
            } else {
                System.out.println("DEBUG PC Item NO Actualizado (Precio no provisto): ID=" + itemId);
            }
        }

        pedido.setEstado(EstadoPedidoCotizacion.COTIZADO);
        PedidoCotizacion pedidoGuardado = pedidoCotizacionRepository.save(pedido);
        System.out.println("DEBUG PedidoCotizacionService - Después de guardar: Pedido ID=" + pedidoGuardado.getId() + ", Estado=" + pedidoGuardado.getEstado());

        // Cambiar el estado de la SolicitudCompra a COTIZADA cuando se carga la cotización
        if (pedidoGuardado.getSolicitudCompra() != null) {
            SolicitudCompra solicitud = pedidoGuardado.getSolicitudCompra();
            if (solicitud.getEstado() == EstadoSolicitud.COTIZANDO) {
                solicitud.setEstado(EstadoSolicitud.COTIZADA);
                solicitudCompraRepository.save(solicitud);
                System.out.println("DEBUG - SolicitudCompra ID=" + solicitud.getId() + " cambiada a estado COTIZADA");
            }
        }

        return pedidoGuardado;
    }

    public List<PedidoCotizacion> getAllPedidosCotizacion() {
        return pedidoCotizacionRepository.findAll();
    }

    public Optional<PedidoCotizacion> getPedidoCotizacionById(Long id) {
        return pedidoCotizacionRepository.findById(id)
                .map(pedido -> {
                    Hibernate.initialize(pedido.getItems()); // Forzar inicialización de la colección
                    return pedido;
                });
    }
}
