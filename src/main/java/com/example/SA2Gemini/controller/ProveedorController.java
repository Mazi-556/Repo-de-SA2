package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.Proveedor;
import com.example.SA2Gemini.repository.RubroRepository; // Import RubroRepository
import com.example.SA2Gemini.repository.TipoProveedorRepository; // Import TipoProveedorRepository
import com.example.SA2Gemini.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private RubroRepository rubroRepository; // Inject RubroRepository

    @Autowired
    private TipoProveedorRepository tipoProveedorRepository; // Inject TipoProveedorRepository

    @GetMapping
    public String listarProveedores(Model model) {
        model.addAttribute("proveedores", proveedorService.findAll());
        return "proveedores";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeAlta(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("titulo", "Alta de Proveedor");
        model.addAttribute("tiposProveedor", tipoProveedorRepository.findAll()); // Add entities to model
        model.addAttribute("rubros", rubroRepository.findAll()); // Add rubros to model
        return "proveedor-form";
    }

    @PostMapping("/guardar")
    public String guardarProveedor(@ModelAttribute Proveedor proveedor) {
        proveedorService.save(proveedor);
        return "redirect:/proveedores";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicion(@PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorService.findById(id);
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("titulo", "Edición de Proveedor");
        model.addAttribute("tiposProveedor", tipoProveedorRepository.findAll()); // Add entities to model
        model.addAttribute("rubros", rubroRepository.findAll()); // Add rubros to model
        return "proveedor-form";
    }

    @PostMapping("/desactivar/{id}")
    public String desactivarProveedor(@PathVariable Long id) {
        Proveedor proveedor = proveedorService.findById(id);
        if (proveedor != null) {
            proveedor.setActivo(false); // Set to inactive
            proveedorService.save(proveedor);
        }
        return "redirect:/proveedores"; // Redirect back to the provider list
    }

    @PostMapping("/activar/{id}")
    public String activarProveedor(@PathVariable Long id) {
        Proveedor proveedor = proveedorService.findById(id);
        if (proveedor != null) {
            proveedor.setActivo(true); // Set to active
            proveedorService.save(proveedor);
        }
        return "redirect:/proveedores"; // Redirect back to the provider list
    }
}
