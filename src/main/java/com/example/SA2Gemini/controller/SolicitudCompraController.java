package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.SolicitudCompra;
import com.example.SA2Gemini.service.ProductoService; // Import ProductoService
import com.example.SA2Gemini.service.SolicitudCompraService;
import org.springframework.beans.factory.annotation.Autowired; // Import Autowired
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/solicitudes-compra")
public class SolicitudCompraController {

    @Autowired
    private SolicitudCompraService solicitudCompraService;

    @Autowired
    private ProductoService productoService; // Inject ProductoService

    @GetMapping
    public String listarSolicitudes(Model model) {
        model.addAttribute("solicitudes", solicitudCompraService.getAllSolicitudesCompra());
        return "solicitud-compra-listado";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        SolicitudCompra solicitud = new SolicitudCompra();
        solicitud.setItems(new java.util.ArrayList<>()); // Explicitly initialize items list
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("productos", productoService.findAll()); // Add products to the model
        return "solicitud-compra-form";
    }

    @PostMapping("/guardar")
    public String guardarSolicitud(@ModelAttribute SolicitudCompra solicitud) {
        solicitudCompraService.saveSolicitudCompra(solicitud);
        return "redirect:/solicitudes-compra";
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("solicitud", solicitudCompraService.getSolicitudCompraById(id).orElse(null));
        return "solicitud-compra-detalle";
    }
}
