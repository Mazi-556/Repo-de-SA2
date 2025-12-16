package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.EstadoOrdenCompra;
import com.example.SA2Gemini.entity.OrdenCompra;
import com.example.SA2Gemini.service.OrdenCompraService;
import com.example.SA2Gemini.service.RemitoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/remitos")
public class RemitoController {

    @Autowired
    private OrdenCompraService ordenCompraService;

    @Autowired
    private RemitoService remitoService;

    @GetMapping
    public String listarOrdenesDeCompraParaRemito(Model model) {
        List<OrdenCompra> ordenes = ordenCompraService.getAllOrdenesCompra().stream()
                .filter(oc -> oc.getEstado() == EstadoOrdenCompra.EMITIDA || oc.getEstado() == EstadoOrdenCompra.RECIBIDA_PARCIAL)
                .filter(oc -> !remitoService.getPendingItems(oc).isEmpty()) // Solo mostrar si hay items pendientes
                .collect(Collectors.toList());
        model.addAttribute("ordenes", ordenes);
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
                                  @RequestParam("itemIds") List<Long> itemIds,
                                  @RequestParam("cantidades") List<Integer> cantidades) {

        remitoService.createRemito(ocId, fechaRemito, itemIds, cantidades);
        return "redirect:/remitos";
    }
}
