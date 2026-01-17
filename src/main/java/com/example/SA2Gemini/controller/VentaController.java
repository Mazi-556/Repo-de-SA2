package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.Venta;
import com.example.SA2Gemini.service.ProductoService;
import com.example.SA2Gemini.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@PreAuthorize("hasAnyRole('COMERCIAL', 'ADMIN')")
@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private VentaService ventaService;

    @GetMapping
    public String listarProductosParaVenta(Model model) {
        model.addAttribute("productos", productoService.findAll());
        return "ventas-productos";
    }
    
    @GetMapping("/historial")
    public String verHistorialVentas(Model model) {
        model.addAttribute("ventas", ventaService.getAllVentas());
        return "ventas-historial";
    }

    @PostMapping("/registrar")
    public String registrarVenta(@RequestParam Long productoId, 
                             @RequestParam Integer cantidad,
                             @RequestParam String formaPago, // Agregamos este parámetro
                             RedirectAttributes redirectAttributes) {
        try {
            ventaService.registrarVenta(productoId, cantidad, formaPago); // Se lo pasamos al servicio            
            redirectAttributes.addFlashAttribute("success", "Venta registrada exitosamente. Stock actualizado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas";
    }
}
