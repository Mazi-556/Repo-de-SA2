package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.EstadoOrdenCompra;
import com.example.SA2Gemini.entity.OrdenCompra;
import com.example.SA2Gemini.entity.Remito;
import com.example.SA2Gemini.service.OrdenCompraService;
import com.example.SA2Gemini.service.RemitoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/remitos")
public class RemitoController {

    @Autowired
    private OrdenCompraService ordenCompraService;

    @Autowired
    private RemitoService remitoService;

    @GetMapping
    public String listarOrdenesDeCompraParaRemito(@RequestParam(name = "archivados", required = false, defaultValue = "false") boolean archivados, Model model) {
        List<OrdenCompra> ordenes;
        if (archivados) {
            ordenes = ordenCompraService.getAllOrdenesCompra().stream()
                    .filter(oc -> oc.getEstado() == EstadoOrdenCompra.RECIBIDA_COMPLETA)
                    .collect(Collectors.toList());
        } else {
            ordenes = ordenCompraService.getAllOrdenesCompra().stream()
                    .filter(oc -> oc.getEstado() == EstadoOrdenCompra.EMITIDA || oc.getEstado() == EstadoOrdenCompra.EN_PROCESO || oc.getEstado() == EstadoOrdenCompra.RECIBIDA_PARCIAL)
                    .filter(oc -> !remitoService.getPendingItems(oc).isEmpty()) // Solo mostrar si hay items pendientes
                    .collect(Collectors.toList());
        }
        model.addAttribute("ordenes", ordenes);
        model.addAttribute("archivados", archivados);
        return "remito-listado-oc";
    }

    @GetMapping("/nuevo/{ocId}")
    public String mostrarFormularioRemito(@PathVariable Long ocId, Model model) {
        OrdenCompra ordenCompra = ordenCompraService.getOrdenCompraById(ocId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid orden de compra Id:" + ocId));

        model.addAttribute("ordenCompra", ordenCompra);
        model.addAttribute("pendingItems", remitoService.getPendingItems(ordenCompra));
        return "remito-form";
    }

    @PostMapping("/confirmar")
    public String confirmarRemito(@RequestParam("ocId") Long ocId,
                                @RequestParam("fechaRemito") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaRemito,
                                @RequestParam MultiValueMap<String, String> allParams) {

        Map<Long, Integer> itemQuantities = new HashMap<>();
        
        // Recorremos los parámetros para capturar las cantidades ingresadas
        for (String key : allParams.keySet()) {
            if (key.startsWith("cantidades[")) {
                String idStr = key.substring(key.indexOf("[") + 1, key.indexOf("]"));
                Long itemId = Long.parseLong(idStr);
                
                try {
                    // Obtenemos la cantidad y, si es mayor a 0, la agregamos al mapa
                    String value = allParams.getFirst(key);
                    if (value != null && !value.isEmpty()) {
                        int quantity = Integer.parseInt(value);
                        if (quantity > 0) {
                            itemQuantities.put(itemId, quantity);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Si no es un número válido, simplemente lo ignoramos
                }
            }
        }

        // ESTO ES LO QUE DEBES MANTENER:
        // Enviamos los datos al servicio para crear el remito y actualizar stock
        remitoService.createRemito(ocId, fechaRemito, itemQuantities);
        
        // Volvemos a la lista de remitos
        return "redirect:/remitos";
    }

    @GetMapping("/orden/{id}")
    public String listarRemitosPorOrden(@PathVariable Long id, Model model) {
        OrdenCompra ordenCompra = ordenCompraService.getOrdenCompraById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid orden de compra Id:" + id));
        model.addAttribute("ordenCompra", ordenCompra);
        model.addAttribute("remitos", remitoService.getRemitosByOrdenCompraId(id));
        return "remito-listado";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalleRemito(@PathVariable Long id, Model model) {
        Remito remito = remitoService.getRemitoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid remito Id:" + id));
        model.addAttribute("remito", remito);
        return "remito-detalle";
    }
}
