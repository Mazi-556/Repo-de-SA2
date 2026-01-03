package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.PedidoCotizacion;
import com.example.SA2Gemini.entity.PedidoCotizacionItem;
import com.example.SA2Gemini.service.PedidoCotizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Importar PathVariable
import org.springframework.web.bind.annotation.PostMapping; // Importar PostMapping
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Importar RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Importar RedirectAttributes
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal; // Importar BigDecimal
import java.time.LocalDate;
import java.util.HashMap; // Importar HashMap
import java.util.List;
import java.util.Map;

@PreAuthorize("hasAnyRole('COMPRAS', 'ADMIN')")
@Controller
@RequestMapping("/pedidos-cotizacion")
public class PedidoCotizacionController {

    @Autowired
    private PedidoCotizacionService pedidoCotizacionService;

    @GetMapping
    public String listarPedidosCotizacion(Model model) {
        List<PedidoCotizacion> pedidosCotizacion = pedidoCotizacionService.getAllPedidosCotizacion();
        model.addAttribute("pedidosCotizacion", pedidosCotizacion);
        return "pedidos-cotizacion-listado";
    }

    @GetMapping("/{id}")
    public String verDetallesPedidoCotizacion(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return pedidoCotizacionService.getPedidoCotizacionById(id)
                .map(pedido -> {
                    model.addAttribute("pedido", pedido);
                    return "pedido-cotizacion-detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Pedido de Cotización no encontrado.");
                    return "redirect:/pedidos-cotizacion";
                });
    }

    @GetMapping("/{id}/cargar-cotizacion")
    public String cargarCotizacion(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return pedidoCotizacionService.getPedidoCotizacionById(id)
                .map(pedido -> {
                    model.addAttribute("pedido", pedido);
                    return "pedido-cotizacion-cargar-cotizacion";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Pedido de Cotización no encontrado para cargar cotización.");
                    return "redirect:/pedidos-cotizacion";
                });
    }

    @PostMapping("/{id}/guardar-cotizacion")
    public String guardarCotizacion(@PathVariable Long id, 
                                    @RequestParam Map<String, String> allParams,
                                    RedirectAttributes redirectAttributes) {
        try {
            Map<Long, BigDecimal> preciosCotizados = new HashMap<>();
            Map<Long, Integer> cantidadesFinales = new HashMap<>();

            // Iterar sobre los parámetros para encontrar los precios y cantidades
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String paramName = entry.getKey();
                String paramValue = entry.getValue();

                if (paramName.startsWith("precio_")) {
                    Long itemId = Long.parseLong(paramName.substring(7)); // "precio_".length()
                    preciosCotizados.put(itemId, new BigDecimal(paramValue));
                } else if (paramName.startsWith("cantidad_")) {
                    Long itemId = Long.parseLong(paramName.substring(9)); // "cantidad_".length()
                    cantidadesFinales.put(itemId, Integer.parseInt(paramValue));
                }
            }

            pedidoCotizacionService.cargarCotizacionProveedor(id, preciosCotizados, cantidadesFinales);
            redirectAttributes.addFlashAttribute("successMessage", "Cotización guardada exitosamente para el Pedido " + id);
        } catch (Exception e) {
            System.err.println("Error al guardar cotización: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar cotización: " + e.getMessage());
        }
        return "redirect:/pedidos-cotizacion";
    }

    @GetMapping("/{id}/descargar-pdf")
    public ResponseEntity<byte[]> descargarPdfPedidoCotizacion(@PathVariable Long id) {
        try {
            PedidoCotizacion pedido = pedidoCotizacionService.getPedidoCotizacionById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido de Cotización no encontrado con ID: " + id));

            // Construir HTML para el PDF
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/>");
            htmlBuilder.append("<style>");
            htmlBuilder.append("body { font-family: Arial, sans-serif; margin: 20px; }");
            htmlBuilder.append("h1 { color: #333; }");
            htmlBuilder.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            htmlBuilder.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            htmlBuilder.append("th { background-color: #4CAF50; color: white; }");
            htmlBuilder.append("</style>");
            htmlBuilder.append("</head><body>");
            htmlBuilder.append("<h1>Pedido de Cotización #").append(pedido.getId()).append("</h1>");
            htmlBuilder.append("<p><strong>Fecha:</strong> ").append(pedido.getFecha()).append("</p>");
            htmlBuilder.append("<p><strong>Proveedor:</strong> ").append(pedido.getProveedor().getNombre()).append("</p>");
            htmlBuilder.append("<p><strong>Estado:</strong> ").append(pedido.getEstado()).append("</p>");

            htmlBuilder.append("<table>");
            htmlBuilder.append("<thead><tr>");
            htmlBuilder.append("<th>Código</th>");
            htmlBuilder.append("<th>Nombre</th>");
            htmlBuilder.append("<th>Marca</th>");
            htmlBuilder.append("<th>Modelo</th>");
            htmlBuilder.append("<th>Cantidad</th>");
            htmlBuilder.append("</tr></thead>");
            htmlBuilder.append("<tbody>");

            for (PedidoCotizacionItem item : pedido.getItems()) {
                htmlBuilder.append("<tr>");
                htmlBuilder.append("<td>").append(item.getProducto().getCodigo()).append("</td>");
                htmlBuilder.append("<td>").append(item.getProducto().getNombre()).append("</td>");
                htmlBuilder.append("<td>").append(item.getProducto().getMarca()).append("</td>");
                htmlBuilder.append("<td>").append(item.getProducto().getModelo()).append("</td>");
                htmlBuilder.append("<td>").append(item.getCantidad()).append("</td>");
                htmlBuilder.append("</tr>");
            }

            htmlBuilder.append("</tbody></table>");
            htmlBuilder.append("<br/><br/>");
            htmlBuilder.append("<p style='font-size: 12px; color: #666;'>Documento generado el ").append(LocalDate.now()).append("</p>");
            htmlBuilder.append("</body></html>");

            // Generar PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlBuilder.toString());
            renderer.layout();
            renderer.createPDF(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "pedido_cotizacion_" + pedido.getId() + "_" + pedido.getProveedor().getNombre().replace(" ", "_") + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok().headers(headers).body(outputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(("Error al generar PDF: " + e.getMessage()).getBytes());
        }
    }
}
