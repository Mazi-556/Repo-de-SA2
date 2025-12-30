package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.EstadoSolicitud;
import com.example.SA2Gemini.entity.OrdenCompra;
import com.example.SA2Gemini.entity.OrdenCompraItem;
import com.example.SA2Gemini.entity.PedidoCotizacionItem;
import com.example.SA2Gemini.entity.Proveedor;
import com.example.SA2Gemini.entity.SolicitudCompra;
import com.example.SA2Gemini.entity.SolicitudCompraItem;
import com.example.SA2Gemini.repository.OrdenCompraRepository;
import com.example.SA2Gemini.repository.PedidoCotizacionItemRepository;
import com.example.SA2Gemini.repository.ProveedorRepository;
import com.example.SA2Gemini.repository.SolicitudCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate; // Importar LocalDate
import java.util.List;
import java.util.Map; // Importar Map
import java.util.HashMap; // Importar HashMap
import java.util.ArrayList; // Importar ArrayList
import java.util.Optional;

import org.hibernate.Hibernate; // Importar Hibernate

import org.springframework.http.HttpHeaders; // Importar HttpHeaders
import org.springframework.http.MediaType; // Importar MediaType
import org.springframework.http.ResponseEntity; // Importar ResponseEntity
import org.xhtmlrenderer.pdf.ITextRenderer; // Importar ITextRenderer

import java.io.ByteArrayOutputStream; // Importar ByteArrayOutputStream

@Service
public class OrdenCompraService {

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;
    @Autowired
    private SolicitudCompraRepository solicitudCompraRepository;
    @Autowired
    private ProveedorRepository proveedorRepository;
    @Autowired
    private SolicitudCompraService solicitudCompraService;
    @Autowired
    private PedidoCotizacionItemRepository pedidoCotizacionItemRepository;

    @Transactional
    public OrdenCompra generarOrdenCompra(
            Long proveedorId,
            Long solicitudCompraId, // Opcional, si se quiere vincular a una solicitud específica
            List<Long> solicitudCompraItemIds,
            String formaPago,
            String plazoPago,
            boolean conEnvio,
            Map<Long, Integer> cantidadesFinalesOC,
            Map<Long, BigDecimal> preciosUnitariosFinalesOC) {

        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + proveedorId));

        SolicitudCompra solicitudCompra = null;
        if (solicitudCompraId != null) {
            solicitudCompra = solicitudCompraRepository.findById(solicitudCompraId)
                    .orElseThrow(() -> new RuntimeException("Solicitud de Compra no encontrada con ID: " + solicitudCompraId));
        }

        OrdenCompra ordenCompra = new OrdenCompra();
        ordenCompra.setProveedor(proveedor);
        ordenCompra.setSolicitudCompra(solicitudCompra); // Puede ser null
        ordenCompra.setFormaPago(formaPago);
        ordenCompra.setPlazoPago(plazoPago);
        ordenCompra.setConEnvio(conEnvio);

        // 1. Buscamos los ítems de COTIZACIÓN (que es lo que viene del formulario)
        List<PedidoCotizacionItem> pcItems = pedidoCotizacionItemRepository.findAllById(solicitudCompraItemIds);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (PedidoCotizacionItem pcItem : pcItems) {
            OrdenCompraItem ocItem = new OrdenCompraItem();
            ocItem.setProducto(pcItem.getProducto());
            
            // Obtenemos cantidad y precio del mapa (allParams) usando el ID del ítem de cotización
            Integer cantidadFinal = cantidadesFinalesOC.get(pcItem.getId());
            BigDecimal precioUnitarioFinal = preciosUnitariosFinalesOC.get(pcItem.getId());

            ocItem.setCantidad(cantidadFinal != null ? cantidadFinal : pcItem.getCantidad());
            ocItem.setPrecioUnitario(precioUnitarioFinal != null ? precioUnitarioFinal : pcItem.getPrecioUnitarioCotizado());
            ocItem.setTotal(ocItem.getPrecioUnitario().multiply(new BigDecimal(ocItem.getCantidad())));

            // IMPORTANTE: Actualizar el PedidoCotizacionItem con los precios cargados
            if (precioUnitarioFinal != null) {
                pcItem.setPrecioUnitarioCotizado(precioUnitarioFinal);
                pcItem.setTotalItemCotizado(precioUnitarioFinal.multiply(new BigDecimal(cantidadFinal != null ? cantidadFinal : pcItem.getCantidad())));
                pedidoCotizacionItemRepository.save(pcItem);
            }

            ordenCompra.addOrderItem(ocItem);
            subtotal = subtotal.add(ocItem.getTotal());

            // 2. Intentamos recuperar la Solicitud de Compra para actualizar el estado
            if (solicitudCompra == null && pcItem.getPedidoCotizacion().getSolicitudCompra() != null) {
                solicitudCompra = pcItem.getPedidoCotizacion().getSolicitudCompra();
            }
        }

        // ... (seguir con el cálculo de IVA y el guardado)

        if (solicitudCompra != null) {
            solicitudCompra.setEstado(EstadoSolicitud.COMPROMETIDA);
            solicitudCompraRepository.save(solicitudCompra);
        }


        ordenCompra.setSubtotal(subtotal);
        ordenCompra.setIva(new BigDecimal("0.21").multiply(subtotal)); // Asumo IVA 21%
        ordenCompra.setTotal(subtotal.add(ordenCompra.getIva()));

        System.out.println("DEBUG OC Totales ANTES SAVE: Subtotal=" + ordenCompra.getSubtotal() + ", IVA=" + ordenCompra.getIva() + ", Total=" + ordenCompra.getTotal() + ", Items Size=" + ordenCompra.getItems().size());

        ordenCompra = ordenCompraRepository.save(ordenCompra); // Guardar OrdenCompra principal
        System.out.println("DEBUG OC AFTER MAIN SAVE: Orden ID=" + ordenCompra.getId() + ", Total=" + ordenCompra.getTotal() + ", Items Size=" + ordenCompra.getItems().size());
        
        ordenCompraRepository.flush(); // Forzar el commit a la DB

  

        // Recuperar la orden recién guardada para verificar
        OrdenCompra savedOrden = ordenCompraRepository.findById(ordenCompra.getId()).orElse(null);
        if (savedOrden != null) {
            Hibernate.initialize(savedOrden.getItems()); // Asegurar que los ítems se carguen
            System.out.println("DEBUG after flush & retrieve: Saved Orden ID=" + savedOrden.getId() + ", Total=" + savedOrden.getTotal() + ", Items Size=" + savedOrden.getItems().size());
        }

        if (solicitudCompra != null) {
            solicitudCompra.setEstado(EstadoSolicitud.COMPROMETIDA); 
            solicitudCompraRepository.save(solicitudCompra);
        }

        return ordenCompra;
    }

    public List<OrdenCompra> getAllOrdenesCompra() {
        // En el listado, también asegurarnos de que los ítems se carguen EAGERLY
        List<OrdenCompra> ordenes = ordenCompraRepository.findAll();
        ordenes.forEach(orden -> Hibernate.initialize(orden.getItems()));
        return ordenes;
    }

    public Optional<OrdenCompra> getOrdenCompraById(Long id) {
        System.out.println("DEBUG Service getById: Recuperando OrdenCompra ID: " + id);
        return ordenCompraRepository.findById(id)
                .map(orden -> {
                    Hibernate.initialize(orden.getItems()); // Forzar inicialización de la colección
                    System.out.println("DEBUG Service getById: Orden ID=" + orden.getId() + ", Total=" + orden.getTotal() + ", Items size after initialize: " + orden.getItems().size());
                    return orden;
                });
    }

    public ResponseEntity<byte[]> generarPedidoCotizacionPdf(Long proveedorId, Map<Long, Integer> itemCantidades) throws Exception {
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + proveedorId));

        List<Long> selectedItemIds = new ArrayList<>(itemCantidades.keySet());
        List<SolicitudCompraItem> selectedItems = solicitudCompraService.getSolicitudCompraItemsByIds(selectedItemIds);

        // Agrupar ítems seleccionados por proveedor (aunque ya viene filtrado por un proveedor en este método)
        // Esto es para mantener la estructura de datos que se renderizará en el PDF
        Map<Proveedor, List<SolicitudCompraItem>> presupuestosAgrupados = new HashMap<>();
        presupuestosAgrupados.put(proveedor, new ArrayList<>()); // Inicializar para el proveedor actual

        for (SolicitudCompraItem item : selectedItems) {
            // Verificar si el ítem corresponde al proveedor para el que se está generando la cotización
            // Esto es crucial para evitar mostrar ítems que no pueden ser provistos por este proveedor
            boolean isProvierForProduct = item.getProducto().getProductoProveedores().stream()
                                            .anyMatch(pp -> pp.getProveedor().getId().equals(proveedorId));
            
            if (isProvierForProduct) {
                // Actualizar la cantidad del ítem según lo que el usuario seleccionó en la interfaz
                item.setCantidad(itemCantidades.getOrDefault(item.getId(), item.getCantidad()));
                presupuestosAgrupados.get(proveedor).add(item);
            }
        }
        
        // Construir HTML para el PDF
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/></head><body>");
        htmlBuilder.append("<h1>Pedido de Cotización</h1>");
        htmlBuilder.append("<p>Fecha: ").append(LocalDate.now()).append("</p>");
        htmlBuilder.append("<h2>Para Proveedor: ").append(proveedor.getNombre()).append("</h2>");

        htmlBuilder.append("<table border='1' cellpadding='5' cellspacing='0' style='width:100%; border-collapse: collapse;'>");
        htmlBuilder.append("<thead><tr><th>Código</th><th>Nombre</th><th>Marca</th><th>Modelo</th><th>Cantidad</th></tr></thead>");
        htmlBuilder.append("<tbody>");

        for (SolicitudCompraItem item : presupuestosAgrupados.get(proveedor)) {
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td>").append(item.getProducto().getCodigo()).append("</td>");
            htmlBuilder.append("<td>").append(item.getProducto().getNombre()).append("</td>");
            htmlBuilder.append("<td>").append(item.getProducto().getMarca()).append("</td>");
            htmlBuilder.append("<td>").append(item.getProducto().getModelo()).append("</td>"); // Se corrige a getModelo()
            htmlBuilder.append("<td>").append(item.getCantidad()).append("</td>");
            htmlBuilder.append("</tr>");
        }
        htmlBuilder.append("</tbody></table>");
        htmlBuilder.append("</body></html>");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlBuilder.toString());
        renderer.layout();
        renderer.createPDF(outputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "pedido_cotizacion_" + proveedor.getNombre().replace(" ", "_") + ".pdf");

        return ResponseEntity.ok().headers(headers).body(outputStream.toByteArray());
    }
}
