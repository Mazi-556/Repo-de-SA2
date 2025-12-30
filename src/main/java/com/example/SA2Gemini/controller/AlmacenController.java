package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.Almacen;
import com.example.SA2Gemini.repository.AlmacenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/almacenes")
public class AlmacenController {

    @Autowired
    private AlmacenRepository almacenRepository;

    @GetMapping
    public String listarAlmacenes(Model model) {
        model.addAttribute("almacenes", almacenRepository.findAll());
        return "almacenes-listado";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("almacen", new Almacen());
        return "almacen-form";
    }

    @PostMapping("/guardar")
    public String guardarAlmacen(@ModelAttribute Almacen almacen, Model model) {
        if (almacen.getNombre() == null || almacen.getNombre().trim().isEmpty()) {
            model.addAttribute("error", "El nombre del almacén es obligatorio.");
            model.addAttribute("almacen", almacen);
            return "almacen-form";
        }
        
        almacenRepository.save(almacen);
        return "redirect:/almacenes";
    }

    @GetMapping("/editar/{id}")
    public String editarAlmacen(@PathVariable Long id, Model model) {
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));
        model.addAttribute("almacen", almacen);
        return "almacen-form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarAlmacen(@PathVariable Long id) {
        almacenRepository.deleteById(id);
        return "redirect:/almacenes";
    }
}
