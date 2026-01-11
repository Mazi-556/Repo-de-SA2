package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.PedidoCotizacion;
import com.example.SA2Gemini.service.PedidoCotizacionService;
import com.example.SA2Gemini.service.OrdenCompraService; // Necesario para generar la OrdenCompra
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map; // Para recibir itemCantidades
import java.util.HashMap; // Importar HashMap
import java.math.BigDecimal; // Importar BigDecimal

@PreAuthorize("hasAnyRole('COMPRAS', 'ALMACEN', 'ADMIN')")
@Controller
@RequestMapping("/ordenes-compra")
public class OrdenCompraController {

    @Autowired
    private PedidoCotizacionService pedidoCotizacionService;

    @Autowired
    private OrdenCompraService ordenCompraService;

    @GetMapping
    public String mostrarPaginaUnificada(Model model) {
        // Cargar pedidos de cotización para la sección "Generar"
        List<PedidoCotizacion> pedidosCotizacion = pedidoCotizacionService.getAllPedidosCotizacion();
        
        // Filtrar solo pedidos que tengan cotización cargada (al menos un ítem con precio)
        List<PedidoCotizacion> pedidosConCotizacion = pedidosCotizacion.stream()
            .filter(pedido -> pedido.getItems().stream()
                .anyMatch(item -> item.getPrecioUnitarioCotizado() != null && 
                                  item.getPrecioUnitarioCotizado().compareTo(java.math.BigDecimal.ZERO) > 0))
            .collect(java.util.stream.Collectors.toList());
        
        // Cargar órdenes de compra para la sección "Listado"
        List<com.example.SA2Gemini.entity.OrdenCompra> ordenesCompra = ordenCompraService.getAllOrdenesCompra();
        
        model.addAttribute("pedidosCotizacion", pedidosConCotizacion);
        model.addAttribute("pedidoSeleccionado", null);
        model.addAttribute("ordenesCompra", ordenesCompra);
        
        return "orden-compra-unified";
    }

    @GetMapping("/{id}")
    public String seleccionarPedidoCotizacion(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        List<PedidoCotizacion> pedidosCotizacion = pedidoCotizacionService.getAllPedidosCotizacion();
        
        // Filtrar solo pedidos que tengan cotización cargada
        List<PedidoCotizacion> pedidosConCotizacion = pedidosCotizacion.stream()
            .filter(pedido -> pedido.getItems().stream()
                .anyMatch(item -> item.getPrecioUnitarioCotizado() != null && 
                                  item.getPrecioUnitarioCotizado().compareTo(java.math.BigDecimal.ZERO) > 0))
            .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("pedidosCotizacion", pedidosConCotizacion);

        return pedidoCotizacionService.getPedidoCotizacionById(id)
                .map(pedido -> {
                    // Validar que el pedido tenga cotización cargada
                    boolean tieneCotizacion = pedido.getItems().stream()
                        .anyMatch(item -> item.getPrecioUnitarioCotizado() != null && 
                                          item.getPrecioUnitarioCotizado().compareTo(java.math.BigDecimal.ZERO) > 0);
                    
                    if (!tieneCotizacion) {
                        redirectAttributes.addFlashAttribute("errorMessage", 
                            "Este pedido de cotización no tiene precios cargados. Debe cargar la cotización primero desde 'Pedidos de Cotización'.");
                        return "redirect:/ordenes-compra";
                    }
                    
                    model.addAttribute("pedidoSeleccionado", pedido);
                    return "orden-compra-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Pedido de Cotización no encontrado.");
                    return "redirect:/ordenes-compra";
                });
    }

    @PostMapping("/generar")
    public String generarOrdenCompraDesdePedidoCotizacion(
            @RequestParam Long pedidoCotizacionId,
            @RequestParam(name = "itemIdsOrdenCompra", required = false) List<Long> itemIdsOrdenCompra,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("DEBUG Controller: itemIdsOrdenCompra recibido del formulario: " + itemIdsOrdenCompra);
        System.out.println("DEBUG Controller: allParams recibido del formulario: " + allParams);
        System.out.println("--- Generar Orden de Compra desde Pedido de Cotización ID: " + pedidoCotizacionId + " ---");

        // Validación: debe haber al menos un ítem seleccionado
        if (itemIdsOrdenCompra == null || itemIdsOrdenCompra.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar al menos un producto para generar la Orden de Compra.");
            return "redirect:/ordenes-compra/" + pedidoCotizacionId;
        }

        // Mapas para almacenar las cantidades y precios finales editados por el usuario para la OC
        Map<Long, Integer> cantidadesFinalesOC = new HashMap<>();
        Map<Long, BigDecimal> preciosUnitariosFinalesOC = new HashMap<>();

        if (itemIdsOrdenCompra != null) {
            for (Long itemId : itemIdsOrdenCompra) {
                // Recuperar cantidad final
                String cantidadKey = "cantidad_oc_item_" + itemId;
                if (allParams.containsKey(cantidadKey)) {
                    try {
                        cantidadesFinalesOC.put(itemId, Integer.parseInt(allParams.get(cantidadKey)));
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear cantidad final para item " + itemId + ": " + allParams.get(cantidadKey));
                        redirectAttributes.addFlashAttribute("errorMessage", "Error: Cantidad inválida para el ítem " + itemId);
                        return "redirect:/ordenes-compra/" + pedidoCotizacionId;
                    }
                } else {
                    // Si el ítem está seleccionado pero no se envía cantidad, es un error o default
                    redirectAttributes.addFlashAttribute("errorMessage", "Error: Cantidad no especificada para el ítem " + itemId);
                    return "redirect:/ordenes-compra/" + pedidoCotizacionId;
                }

                // Recuperar precio unitario final
                String precioKey = "precio_oc_item_" + itemId;
                if (allParams.containsKey(precioKey)) {
                    try {
                        preciosUnitariosFinalesOC.put(itemId, new BigDecimal(allParams.get(precioKey)));
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear precio final para item " + itemId + ": " + allParams.get(precioKey));
                        redirectAttributes.addFlashAttribute("errorMessage", "Error: Precio unitario inválido para el ítem " + itemId);
                        return "redirect:/ordenes-compra/" + pedidoCotizacionId;
                    }
                } else {
                    // Si el ítem está seleccionado pero no se envía precio, es un error o default
                    redirectAttributes.addFlashAttribute("errorMessage", "Error: Precio unitario no especificado para el ítem " + itemId);
                    return "redirect:/ordenes-compra/" + pedidoCotizacionId;
                }
            }
        }

        try {
            PedidoCotizacion pedido = pedidoCotizacionService.getPedidoCotizacionById(pedidoCotizacionId)
                                        .orElseThrow(() -> new RuntimeException("Pedido de Cotización no encontrado."));

            // Validación adicional: verificar que todos los ítems seleccionados tengan precio cotizado
            boolean todosConPrecio = itemIdsOrdenCompra.stream().allMatch(itemId -> 
                preciosUnitariosFinalesOC.get(itemId) != null && 
                preciosUnitariosFinalesOC.get(itemId).compareTo(BigDecimal.ZERO) > 0
            );
            
            if (!todosConPrecio) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Todos los productos seleccionados deben tener un precio unitario mayor a 0.");
                return "redirect:/ordenes-compra/" + pedidoCotizacionId;
            }

            ordenCompraService.generarOrdenCompra(
                pedido.getProveedor().getId(),
                pedido.getSolicitudCompra() != null ? pedido.getSolicitudCompra().getId() : null,
                itemIdsOrdenCompra, // IDs de los ítems de PedidoCotizacionItem para los que se generará OrdenCompraItem
                "Contado", // Placeholder de forma de pago
                "30 días", // Placeholder de plazo de pago
                false, // Placeholder de con envío
                cantidadesFinalesOC, // Cantidades finales editadas
                preciosUnitariosFinalesOC // Precios unitarios finales editados
            );
            
            redirectAttributes.addFlashAttribute("successMessage", "Orden de Compra generada exitosamente para el Pedido de Cotización " + pedidoCotizacionId);
            return "redirect:/ordenes-compra/listado"; // Redirigir al listado de OC

        } catch (Exception e) {
            System.err.println("Error al generar Orden de Compra: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al generar Orden de Compra: " + e.getMessage());
            return "redirect:/ordenes-compra/" + pedidoCotizacionId; // Redirigir al formulario con error
        }
    }

    @GetMapping("/listado")
    public String listarOrdenesCompra(Model model) {
        List<com.example.SA2Gemini.entity.OrdenCompra> ordenesCompra = ordenCompraService.getAllOrdenesCompra();
        model.addAttribute("ordenesCompra", ordenesCompra);
        return "ordenes-compra-listado";
    }

    @GetMapping("/listado/{id}")
    public String verDetallesOrdenCompra(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return ordenCompraService.getOrdenCompraById(id)
                .map(orden -> {
                    System.out.println("DEBUG Controller: Orden ID " + orden.getId() + " total " + orden.getTotal() + " items size " + orden.getItems().size());
                    model.addAttribute("orden", orden);
                    return "orden-compra-detalle";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Orden de Compra no encontrada.");
                    return "redirect:/ordenes-compra/listado";
                });
    }
}