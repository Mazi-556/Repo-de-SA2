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
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
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

    private static final BigDecimal IVA_RATE = new BigDecimal("0.21");

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
        
        // Calcular Subtotal, IVA y Total para mostrar en el formulario
        BigDecimal subtotal = oc.getItems().stream()
                .map(item -> Optional.ofNullable(item.getPrecioUnitario()).orElse(BigDecimal.ZERO).multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal ivaTotal = subtotal.multiply(IVA_RATE);
        BigDecimal totalFactura = subtotal.add(ivaTotal);

        // Preparar datos de ítems para JavaScript, asegurando que los precios sean strings numéricos
        List<Map<String, Object>> itemsData = oc.getItems().stream()
                .map(item -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("cantidad", item.getCantidad());
                    // Usamos toPlainString() para asegurar que BigDecimal se serialice como una cadena numérica simple
                    data.put("precioUnitario", Optional.ofNullable(item.getPrecioUnitario()).orElse(BigDecimal.ZERO).toPlainString());
                    return data;
                })
                .collect(Collectors.toList());

        model.addAttribute("ordenCompra", oc);
        model.addAttribute("cuentas", cuentaService.getAllCuentas());
        
        // Añadir los totales calculados al modelo
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("ivaTotal", ivaTotal);
        model.addAttribute("totalFactura", totalFactura);
        
        // Añadir la tasa de IVA para cálculos unitarios en la vista si es necesario
        model.addAttribute("ivaRate", IVA_RATE);
        
        // Añadir los datos de ítems pre-formateados para JS
        model.addAttribute("itemsData", itemsData);
        
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
        // Si hay un error, necesitamos recargar los datos necesarios para la vista factura-form
        OrdenCompra oc = ordenCompraService.getOrdenCompraById(ocId).orElse(null);
        if (oc != null) {
            // Recalcular y añadir al modelo en caso de error
            BigDecimal subtotal = oc.getItems().stream()
                    .map(item -> Optional.ofNullable(item.getPrecioUnitario()).orElse(BigDecimal.ZERO).multiply(BigDecimal.valueOf(item.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal ivaTotal = subtotal.multiply(IVA_RATE);
            BigDecimal totalFactura = subtotal.add(ivaTotal);

            // Recalcular itemsData para el caso de error
            List<Map<String, Object>> itemsData = oc.getItems().stream()
                .map(item -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("cantidad", item.getCantidad());
                    data.put("precioUnitario", Optional.ofNullable(item.getPrecioUnitario()).orElse(BigDecimal.ZERO).toPlainString());
                    return data;
                })
                .collect(Collectors.toList());

            model.addAttribute("ordenCompra", oc);
            model.addAttribute("cuentas", cuentaService.getAllCuentas());
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("ivaTotal", ivaTotal);
            model.addAttribute("totalFactura", totalFactura);
            model.addAttribute("ivaRate", IVA_RATE);
            model.addAttribute("itemsData", itemsData); // Añadir itemsData
        }
        return "factura-form";
    }
}
