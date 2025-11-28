package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.PedidoCotizacion;
import com.example.SA2Gemini.service.PedidoCotizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Importar PathVariable
import org.springframework.web.bind.annotation.PostMapping; // Importar PostMapping
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Importar RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Importar RedirectAttributes

import java.math.BigDecimal; // Importar BigDecimal
import java.util.HashMap; // Importar HashMap
import java.util.List;
import java.util.Map;

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
}
