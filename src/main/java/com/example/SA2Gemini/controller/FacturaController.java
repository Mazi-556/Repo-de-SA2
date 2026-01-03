package com.example.SA2Gemini.controller;


import com.example.SA2Gemini.entity.EstadoOrdenCompra;
import com.example.SA2Gemini.entity.Factura;
import com.example.SA2Gemini.entity.OrdenCompra;
import com.example.SA2Gemini.entity.Asiento;
import com.example.SA2Gemini.entity.EstadoSolicitud; // Importación añadida
import com.example.SA2Gemini.service.OrdenCompraService;
import com.example.SA2Gemini.service.CuentaService; // Asumiendo que existe
import com.example.SA2Gemini.service.FacturaService;
import com.example.SA2Gemini.repository.AsientoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

@PreAuthorize("hasAnyRole('COMPRAS', 'ADMIN')")
@Controller
@RequestMapping("/facturas")
public class FacturaController {

    @Autowired
    private OrdenCompraService ordenCompraService;
    
    @Autowired
    private CuentaService cuentaService; // Para cargar el plan de cuentas en el combo
    
    @Autowired
    private FacturaService facturaService;
    
    @Autowired
    private AsientoRepository asientoRepository;

    private static final BigDecimal IVA_RATE = new BigDecimal("0.21");

    @GetMapping
    public String listarOrdenesDeCompraParaFactura(@RequestParam(value = "mostrar", required = false, defaultValue = "pendientes") String mostrar,
                                                   Model model) {
        List<OrdenCompra> ordenes;
        
        if ("facturadas".equals(mostrar)) {
            // Mostrar solo las órdenes ya facturadas
            ordenes = ordenCompraService.getAllOrdenesCompra().stream()
                    .filter(oc -> oc.getEstado() == EstadoOrdenCompra.FACTURADA)
                    .collect(Collectors.toList());
        } else {
            // Mostrar solo las pendientes de facturar (recibidas pero no facturadas)
            ordenes = ordenCompraService.getAllOrdenesCompra().stream()
                    .filter(oc -> oc.getEstado() == EstadoOrdenCompra.RECIBIDA_COMPLETA || 
                                 oc.getEstado() == EstadoOrdenCompra.RECIBIDA_PARCIAL)
                    .collect(Collectors.toList());
        }
        
        model.addAttribute("ordenes", ordenes);
        model.addAttribute("mostrar", mostrar);
        return "factura-listado-oc";
    }
    
    @GetMapping("/exito")
    public String mostrarExito(@RequestParam("asientoId") Long asientoId,
                               @RequestParam("facturaNumero") String facturaNumero,
                               Model model) {
        // Obtener el asiento completo con sus movimientos
        Asiento asiento = asientoRepository.findById(asientoId)
                .orElseThrow(() -> new RuntimeException("Asiento no encontrado"));
        
        model.addAttribute("asiento", asiento);
        model.addAttribute("facturaNumero", facturaNumero);
        return "factura-exito";
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
                                       @RequestParam("ivaPorcentaje") java.math.BigDecimal ivaPorcentaje,
                                       Model model) {
        
        System.out.println("========================================");
        System.out.println(">>> CONTROLLER: crearFacturaYAsiento LLAMADO");
        System.out.println(">>> ocId: " + ocId);
        System.out.println(">>> numeroFactura: " + numeroFactura);
        System.out.println(">>> ivaPorcentaje: " + ivaPorcentaje);
        System.out.println("========================================");
                                    
        try {
            OrdenCompra oc = ordenCompraService.getOrdenCompraById(ocId)
                    .orElseThrow(() -> new IllegalArgumentException("OC no encontrada"));

            Factura factura = new Factura();
            factura.setNumeroFactura(numeroFactura);
            factura.setOrdenCompra(oc);
            factura.setFecha(LocalDate.now());

        // Ahora le enviamos también el porcentaje que el usuario eligió
        Factura facturaCreada = facturaService.crearFacturaYAsiento(factura, ivaPorcentaje);
        
        // Redirigir a página de éxito con el ID del asiento creado
        return "redirect:/facturas/exito?asientoId=" + facturaCreada.getAsiento().getId() + 
               "&facturaNumero=" + facturaCreada.getNumeroFactura();
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
}