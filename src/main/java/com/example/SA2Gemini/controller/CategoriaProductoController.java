package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.CategoriaProducto;
import com.example.SA2Gemini.repository.CategoriaProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categorias")
public class CategoriaProductoController {

    @Autowired
    private CategoriaProductoRepository categoriaProductoRepository;

    @GetMapping
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", categoriaProductoRepository.findAll());
        return "categorias-listado";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("categoria", new CategoriaProducto());
        return "categoria-form";
    }

    @PostMapping("/guardar")
    public String guardarCategoria(@ModelAttribute CategoriaProducto categoria, Model model) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            model.addAttribute("error", "El nombre de la categoría es obligatorio.");
            model.addAttribute("categoria", categoria);
            return "categoria-form";
        }
        
        categoriaProductoRepository.save(categoria);
        return "redirect:/categorias";
    }

    @GetMapping("/editar/{id}")
    public String editarCategoria(@PathVariable Long id, Model model) {
        CategoriaProducto categoria = categoriaProductoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        model.addAttribute("categoria", categoria);
        return "categoria-form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Long id) {
        categoriaProductoRepository.deleteById(id);
        return "redirect:/categorias";
    }
}
