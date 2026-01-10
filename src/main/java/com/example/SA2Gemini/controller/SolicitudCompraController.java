package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.EstadoSolicitud;
import com.example.SA2Gemini.entity.SolicitudCompra;
import com.example.SA2Gemini.repository.SolicitudCompraRepository;
import com.example.SA2Gemini.service.ProductoService;
import com.example.SA2Gemini.service.SolicitudCompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@PreAuthorize("hasAnyRole('COMPRAS', 'ADMIN', 'ALMACEN')")
@Controller
@RequestMapping("/solicitudes-compra")
public class SolicitudCompraController {

    @Autowired
    private SolicitudCompraService solicitudCompraService;

    @Autowired
    private SolicitudCompraRepository solicitudCompraRepository;

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listarSolicitudes(@RequestParam(required = false) EstadoSolicitud estado, Model model) {
        List<SolicitudCompra> solicitudes;
        
        if (estado != null) {
            // Filtra por el estado seleccionado
            solicitudes = solicitudCompraRepository.findByEstado(estado);
        } else {
            // Por defecto muestra todas menos las FINALIZADAS para no saturar la vista
            solicitudes = solicitudCompraRepository.findAll().stream()
                    .filter(s -> s.getEstado() != EstadoSolicitud.FINALIZADA)
                    .collect(Collectors.toList());
        }

        model.addAttribute("solicitudes", solicitudes);
        // Esta línea es la que hace que aparezcan todas las opciones en el desplegable
        model.addAttribute("estados", EstadoSolicitud.values());
        model.addAttribute("estadoSeleccionado", estado);
        
        return "solicitud-compra-listado";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        SolicitudCompra solicitud = new SolicitudCompra();
        solicitud.setItems(new java.util.ArrayList<>());
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("productos", productoService.findAll());
        return "solicitud-compra-form";
    }

    @PostMapping("/guardar")
    public String guardarSolicitud(@ModelAttribute SolicitudCompra solicitud, Model model) {
        // Validación: la solicitud debe tener al menos un producto
        if (solicitud.getItems() == null || solicitud.getItems().isEmpty()) {
            model.addAttribute("error", "Debe agregar al menos un producto a la solicitud de compra.");
            model.addAttribute("solicitud", solicitud);
            model.addAttribute("productos", productoService.findAll());
            return "solicitud-compra-form";
        }
        solicitudCompraService.saveSolicitudCompra(solicitud);
        return "redirect:/solicitudes-compra";
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("solicitud", solicitudCompraService.getSolicitudCompraById(id).orElse(null));
        return "solicitud-compra-detalle";
    }
}