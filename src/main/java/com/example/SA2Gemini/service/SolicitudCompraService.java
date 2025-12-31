package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.Producto;
import com.example.SA2Gemini.entity.SolicitudCompra;
import com.example.SA2Gemini.entity.SolicitudCompraItem;
import com.example.SA2Gemini.entity.EstadoSolicitud;
import com.example.SA2Gemini.repository.ProductoRepository;
import com.example.SA2Gemini.repository.SolicitudCompraRepository;
import com.example.SA2Gemini.repository.SolicitudCompraItemRepository; // Importar SolicitudCompraItemRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

@Service
public class SolicitudCompraService {

    @Autowired
    private SolicitudCompraRepository solicitudCompraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SolicitudCompraItemRepository solicitudCompraItemRepository; // Inyectar SolicitudCompraItemRepository

    public SolicitudCompra saveSolicitudCompra(SolicitudCompra solicitudCompra) {
        // Process items if they exist
        if (solicitudCompra.getItems() != null && !solicitudCompra.getItems().isEmpty()) {
            for (SolicitudCompraItem item : solicitudCompra.getItems()) {
                // Ensure the Product is a managed entity
                if (item.getProducto() != null && item.getProducto().getId() != null) {
                    Producto managedProducto = productoRepository.findById(item.getProducto().getId())
                            .orElseThrow(() -> new RuntimeException("Producto con ID " + item.getProducto().getId() + " no encontrado."));
                    item.setProducto(managedProducto);
                } else if (item.getProducto() != null) {
                    // If it's a new product, save it (though typically products should pre-exist)
                    productoRepository.save(item.getProducto());
                }

                // Establish bidirectional relationship
                // This is crucial for JPA to track and persist the new items correctly
                item.setSolicitudCompra(solicitudCompra);
            }
        }
        // Save the SolicitudCompra and its associated items (due to CascadeType.ALL)
        return solicitudCompraRepository.save(solicitudCompra);
    }

    public List<SolicitudCompra> getAllSolicitudesCompra() {
        return solicitudCompraRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<SolicitudCompra> getSolicitudCompraById(Long id) {
        Optional<SolicitudCompra> solicitud = solicitudCompraRepository.findById(id);
        solicitud.ifPresent(s -> Hibernate.initialize(s.getItems()));
        return solicitud;
    }

    public SolicitudCompra updateSolicitudCompra(Long id, SolicitudCompra solicitudCompraDetails) {
        SolicitudCompra solicitudCompra = solicitudCompraRepository.findById(id).orElseThrow(() -> new RuntimeException("Solicitud de Compra no encontrada"));
        solicitudCompra.setFecha(solicitudCompraDetails.getFecha());
        solicitudCompra.setEstado(solicitudCompraDetails.getEstado());
        solicitudCompra.setProveedorSugerido(solicitudCompraDetails.getProveedorSugerido());

        // Actualizar items existentes o agregar nuevos
        solicitudCompra.getItems().clear(); // Limpiar items existentes para reemplazarlos
        solicitudCompraDetails.getItems().forEach(newItem -> {
            newItem.setSolicitudCompra(solicitudCompra);
            solicitudCompra.getItems().add(newItem);
        });

        return solicitudCompraRepository.save(solicitudCompra);
    }

    public void deleteSolicitudCompra(Long id) {
        SolicitudCompra solicitudCompra = solicitudCompraRepository.findById(id).orElseThrow(() -> new RuntimeException("Solicitud de Compra no encontrada"));
        solicitudCompraRepository.delete(solicitudCompra);
    }

    public void crearSolicitudAutomatica(Producto producto) {
        SolicitudCompra solicitudCompra = new SolicitudCompra();
        solicitudCompra.setFecha(LocalDate.now()); // Set current date
        solicitudCompra.setEstado(EstadoSolicitud.PENDIENTE); // Set initial status
        solicitudCompra.setObservaciones("SC generada automáticamente: el producto '" + producto.getNombre() + 
                                         "' alcanzó el punto de reposición (" + producto.getPuntoReposicion() + 
                                         " unidades). Stock al momento de crear esta SC: " + producto.getStockActual());

        SolicitudCompraItem item = new SolicitudCompraItem();
        item.setProducto(producto);
        item.setCantidad(producto.getPuntoReposicion() * 2); // Example: Order double the reorder point
        item.setDescripcion("Reposición automática");
        item.setPrecioUnitario(producto.getPrecioCosto()); // Use product cost as unit price
        item.setSolicitudCompra(solicitudCompra);

        solicitudCompra.setItems(List.of(item)); // Add item to the request

        saveSolicitudCompra(solicitudCompra);
    }

    // Nuevo método para obtener todos los SolicitudCompraItem de solicitudes en estado INICIO
    public List<SolicitudCompraItem> getSolicitudCompraItemsByEstadoInicio() {
        List<SolicitudCompra> solicitudesEnInicio = solicitudCompraRepository.findByEstado(EstadoSolicitud.PENDIENTE);
        return solicitudesEnInicio.stream()
                .flatMap(solicitud -> solicitud.getItems().stream())
                .collect(java.util.stream.Collectors.toList());
    }

    // Nuevo método para obtener SolicitudCompraItem por una lista de IDs
    public List<SolicitudCompraItem> getSolicitudCompraItemsByIds(List<Long> ids) {
        return solicitudCompraItemRepository.findByIdIn(ids);
    }
}
