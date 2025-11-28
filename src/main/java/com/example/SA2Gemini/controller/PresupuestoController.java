package com.example.SA2Gemini.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // Importar PostMapping
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Importar RequestParam
import java.util.List; // Importar List
import java.util.Map; // Importar Map
import java.util.HashMap; // Importar HashMap
import java.util.ArrayList; // Importar ArrayList
import org.springframework.beans.factory.annotation.Autowired; // Importar Autowired
import com.example.SA2Gemini.service.SolicitudCompraService; // Importar SolicitudCompraService
import com.example.SA2Gemini.entity.SolicitudCompraItem; // Importar SolicitudCompraItem
import com.example.SA2Gemini.entity.Proveedor; // Importar Proveedor
import com.example.SA2Gemini.entity.ProductoProveedor; // Importar ProductoProveedor
import com.example.SA2Gemini.service.OrdenCompraService; // Importar OrdenCompraService
import com.example.SA2Gemini.service.PedidoCotizacionService; // Importar PedidoCotizacionService

import org.springframework.http.HttpHeaders; // Importar HttpHeaders
import org.springframework.http.MediaType; // Importar MediaType
import org.springframework.http.ResponseEntity; // Importar ResponseEntity
import org.xhtmlrenderer.pdf.ITextRenderer; // Importar ITextRenderer

import java.io.ByteArrayOutputStream; // Importar ByteArrayOutputStream

@Controller
@RequestMapping("/presupuestos")
public class PresupuestoController {

    public PresupuestoController() {
        // System.out.println("--- PresupuestoController ha sido cargado ---"); // Eliminamos el log de depuración
    }

    @Autowired
    private SolicitudCompraService solicitudCompraService;

    @Autowired
    private OrdenCompraService ordenCompraService; // Inyectar OrdenCompraService

    @Autowired
    private PedidoCotizacionService pedidoCotizacionService; // Inyectar PedidoCotizacionService

    @GetMapping("/pedidos")
    public String listarPedidosParaPresupuesto(Model model) {
        List<SolicitudCompraItem> solicitudItems = solicitudCompraService.getSolicitudCompraItemsByEstadoInicio();
        model.addAttribute("solicitudItems", solicitudItems);
        return "presupuesto-listado-pedidos";
    }

    @PostMapping({"/generar", "/generar/"}) // Mapeo para manejar la generación de presupuestos
    public String generarPresupuestos(@RequestParam(required = false) List<Long> itemIds, Model model) { // Volvemos a incluir @RequestParam
        // System.out.println("--- Método generarPresupuestos (SIMPLIFICADO) invocado ---"); // Eliminamos el log de depuración
        // Lógica completa restaurada
        List<SolicitudCompraItem> selectedItems = solicitudCompraService.getSolicitudCompraItemsByIds(itemIds);

        // Mapa para agrupar ítems por proveedor
        Map<Proveedor, List<SolicitudCompraItem>> presupuestosAgrupados = new HashMap<>();

        for (SolicitudCompraItem item : selectedItems) {
            if (item.getProducto() != null && item.getProducto().getProductoProveedores() != null) {
                for (ProductoProveedor pp : item.getProducto().getProductoProveedores()) {
                    Proveedor proveedor = pp.getProveedor();
                    presupuestosAgrupados
                            .computeIfAbsent(proveedor, k -> new ArrayList<>())
                            .add(item);
                }
            }
        }
        
        model.addAttribute("presupuestosAgrupados", presupuestosAgrupados);
        model.addAttribute("selectedItemIds", itemIds); // Añadir los IDs de ítems seleccionados al modelo
        return "presupuesto-detalle-generado";
    }

    @GetMapping("/generado")
    public String detalleGenerado(Model model) {
        // Este método ahora solo muestra la plantilla. La lógica de carga de datos
        // se hará en generarPresupuestos si viene de un POST, o se necesitaría
        // un mecanismo para mantener los datos en la sesión o pasarlos con redirect attributes.
        // Por ahora, si se accede directamente a /generado, la vista estará vacía.
        return "presupuesto-detalle-generado";
    }

    @PostMapping("/confirmar") // Cambiado a PostMapping para manejar el submit del formulario
    public String confirmarModal(@RequestParam List<Long> itemIds, Model model) { // Añadimos itemIds
        // Re-ejecutar la lógica para agrupar ítems por proveedor
        List<SolicitudCompraItem> selectedItems = solicitudCompraService.getSolicitudCompraItemsByIds(itemIds);

        // Mapa para agrupar ítems por proveedor
        Map<Proveedor, List<SolicitudCompraItem>> presupuestosAgrupados = new HashMap<>();

        for (SolicitudCompraItem item : selectedItems) {
            if (item.getProducto() != null && item.getProducto().getProductoProveedores() != null) {
                for (ProductoProveedor pp : item.getProducto().getProductoProveedores()) {
                    Proveedor proveedor = pp.getProveedor();
                    presupuestosAgrupados
                            .computeIfAbsent(proveedor, k -> new ArrayList<>())
                            .add(item);
                }
            }
        }
        
        model.addAttribute("presupuestosAgrupados", presupuestosAgrupados);
        model.addAttribute("selectedItemIds", itemIds); // Añadir los IDs de ítems seleccionados al modelo
        return "presupuesto-confirmar-modal";
    }

    @PostMapping("/generar-pdf")
    public ResponseEntity<byte[]> generarPdfPresupuesto(@RequestParam List<Long> itemIds) {
        try {
            List<SolicitudCompraItem> selectedItems = solicitudCompraService.getSolicitudCompraItemsByIds(itemIds);

            Map<Proveedor, List<SolicitudCompraItem>> presupuestosAgrupados = new HashMap<>();
            for (SolicitudCompraItem item : selectedItems) {
                if (item.getProducto() != null && item.getProducto().getProductoProveedores() != null) {
                    for (ProductoProveedor pp : item.getProducto().getProductoProveedores()) {
                        Proveedor proveedor = pp.getProveedor();
                        presupuestosAgrupados
                                .computeIfAbsent(proveedor, k -> new ArrayList<>())
                                .add(item);
                    }
                }
            }

            // Construir HTML para el PDF
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/></head><body>");
            htmlBuilder.append("<h1>Presupuesto Generado</h1>");
            htmlBuilder.append("<p>Fecha: ").append(java.time.LocalDate.now()).append("</p>");
            
            for (Map.Entry<Proveedor, List<SolicitudCompraItem>> entry : presupuestosAgrupados.entrySet()) {
                htmlBuilder.append("<h2>Proveedor: ").append(entry.getKey().getNombre()).append("</h2>");
                htmlBuilder.append("<ul>");
                for (SolicitudCompraItem item : entry.getValue()) {
                    htmlBuilder.append("<li>")
                               .append(item.getProducto().getNombre())
                               .append(" (Cantidad: ").append(item.getCantidad())
                               .append(") - Precio Unitario: ").append(item.getPrecioUnitario())
                               .append("</li>");
                }
                htmlBuilder.append("</ul>");
            }
            htmlBuilder.append("</body></html>");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlBuilder.toString());
            renderer.layout();
            renderer.createPDF(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "presupuesto.pdf"); // Forzar descarga

            return ResponseEntity.ok().headers(headers).body(outputStream.toByteArray());

        } catch (Exception e) {
            System.err.println("Error al generar PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null); // Retornar error 500
        }
    }

    @PostMapping("/generar-pedido-cotizacion")
    public ResponseEntity<byte[]> generarPedidoCotizacion(@RequestParam Long proveedorId,
                                                           @RequestParam(name = "selectedItemIds", required = false) List<Long> selectedItemIds,
                                                           @RequestParam Map<String, String> allParams) {
        
        System.out.println("--- Se solicitó generar Pedido de Cotización para Proveedor ID: " + proveedorId + " ---");
        System.out.println("--- IDs de ítems seleccionados: " + selectedItemIds + " ---");
        
        // Reconstruir los ítems con sus cantidades desde allParams
        Map<Long, Integer> itemCantidades = new HashMap<>();
        if (selectedItemIds != null) {
            for (Long itemId : selectedItemIds) {
                String cantidadKey = "cantidad_item_" + itemId;
                if (allParams.containsKey(cantidadKey)) {
                    try {
                        itemCantidades.put(itemId, Integer.parseInt(allParams.get(cantidadKey)));
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear cantidad para item " + itemId + ": " + allParams.get(cantidadKey));
                        // Manejar el error o establecer una cantidad predeterminada
                        itemCantidades.put(itemId, 1);
                    }
                } else {
                    // Si el checkbox está marcado pero la cantidad no se envió, tomar la cantidad original del item o default
                    itemCantidades.put(itemId, 1); // Asumir 1 si no se especifica
                }
            }
        }

        // Llamar al servicio para generar el PDF del pedido de cotización
        try {
            // Guardar el PedidoCotizacion en la base de datos antes de generar el PDF
            pedidoCotizacionService.guardarPedidoCotizacion(proveedorId, itemCantidades);
            
            return ordenCompraService.generarPedidoCotizacionPdf(proveedorId, itemCantidades);
        } catch (Exception e) {
            System.err.println("Error al generar PDF de Pedido de Cotización: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
