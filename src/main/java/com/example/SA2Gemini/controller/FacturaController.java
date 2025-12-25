package com.example.SA2Gemini.controller;


import com.example.SA2Gemini.entity.EstadoOrdenCompra;
import com.example.SA2Gemini.entity.Factura;
import com.example.SA2Gemini.entity.OrdenCompra;
import com.example.SA2Gemini.service.OrdenCompraService;
import com.example.SA2Gemini.service.CuentaService; // Asumiendo que existe
import com.example.SA2Gemini.service.FacturaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/facturas")
public class FacturaController {

    @Autowired
    private OrdenCompraService ordenCompraService;
    
    @Autowired
    private CuentaService cuentaService; // Para cargar el plan de cuentas en el combo
    
    @Autowired
    private FacturaService facturaService;

    @GetMapping
    public String listarOrdenesDeCompraParaFactura(Model model) {
        // Filtramos para mostrar solo lo que ya se recibió (en proceso o recibida)
        List<OrdenCompra> ordenes = ordenCompraService.getAllOrdenesCompra().stream()
                .filter(oc -> oc.getEstado() == EstadoOrdenCompra.RECIBIDA_COMPLETA || 
                             oc.getEstado() == EstadoOrdenCompra.RECIBIDA_PARCIAL)
                .collect(Collectors.toList());
        model.addAttribute("ordenes", ordenes);
        return "factura-listado-oc";
    }

    @GetMapping("/nuevo/{ocId}")
    public String mostrarFormularioFactura(@PathVariable Long ocId, Model model) {
        OrdenCompra oc = ordenCompraService.getOrdenCompraById(ocId)
                .orElseThrow(() -> new IllegalArgumentException("OC no encontrada: " + ocId));
        
        model.addAttribute("ordenCompra", oc);
        model.addAttribute("cuentas", cuentaService.getAllCuentas());
        return "factura-form";
    }

    @PostMapping("/crear-asiento")
public String crearFacturaYAsiento(@RequestParam("ocId") Long ocId,
                                   @RequestParam("numeroFactura") String numeroFactura,
                                   Model model) {
    try {
        OrdenCompra oc = ordenCompraService.getOrdenCompraById(ocId)
                .orElseThrow(() -> new IllegalArgumentException("OC no encontrada"));

        Factura factura = new Factura();
        factura.setNumeroFactura(numeroFactura);
        factura.setOrdenCompra(oc);
        factura.setFecha(LocalDate.now());

        // El servicio se encargará de calcular subtotal, iva y total
        facturaService.crearFacturaYAsiento(factura);

        return "redirect:/facturas?success";
    } catch (Exception e) {
        model.addAttribute("error", "Error al procesar la factura: " + e.getMessage());
        return "factura-form";
    }
}
}