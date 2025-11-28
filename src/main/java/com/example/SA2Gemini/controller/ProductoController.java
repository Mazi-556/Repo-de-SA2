package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.Producto;
import com.example.SA2Gemini.repository.AlmacenRepository;
import com.example.SA2Gemini.repository.CategoriaProductoRepository;
import com.example.SA2Gemini.repository.ProveedorRepository;
import com.example.SA2Gemini.repository.ProductoProveedorRepository; // Importar ProductoProveedorRepository
import com.example.SA2Gemini.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.SA2Gemini.entity.ProductoProveedor; // Importar ProductoProveedor
import com.example.SA2Gemini.entity.Proveedor; // Importar Proveedor
import java.util.Optional; // Importar Optional


@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaProductoRepository categoriaProductoRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductoProveedorRepository productoProveedorRepository; // Inyectar ProductoProveedorRepository

    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.findAll());
        return "productos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDeAlta(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaProductoRepository.findAll());
        model.addAttribute("almacenes", almacenRepository.findAll());
        return "producto-form";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.findById(id));
        model.addAttribute("categorias", categoriaProductoRepository.findAll());
        model.addAttribute("almacenes", almacenRepository.findAll());
        return "producto-form";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto) {
        productoService.save(producto);
        return "redirect:/productos";
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.findById(id));
        // Aquí se cargaría el historial de compras
        return "producto-detalle";
    }

    @GetMapping("/asignar-proveedor/{id}")
    public String asignarProveedor(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.findById(id));
        model.addAttribute("allProveedores", proveedorRepository.findAll()); // Cargar todos los proveedores disponibles
        return "producto-proveedores";
    }

    @PostMapping("/asignar-proveedor")
    public String asignarProveedorPost(@RequestParam Long productoId, @RequestParam Long proveedorId) {
        Producto producto = productoService.findById(productoId);
        Proveedor proveedor = proveedorRepository.findById(proveedorId).orElse(null);

        if (producto != null && proveedor != null) {
            // Verificar si la relación ya existe para evitar duplicados
            boolean alreadyAssigned = producto.getProductoProveedores().stream()
                                            .anyMatch(pp -> pp.getProveedor().getId().equals(proveedorId));
            if (!alreadyAssigned) {
                ProductoProveedor productoProveedor = new ProductoProveedor();
                productoProveedor.setProducto(producto);
                productoProveedor.setProveedor(proveedor);
                productoProveedorRepository.save(productoProveedor);
            }
        }
        return "redirect:/productos/asignar-proveedor/" + productoId;
    }

    @PostMapping("/desasignar-proveedor")
    public String desasignarProveedor(@RequestParam Long productoProveedorId) {
        productoProveedorRepository.deleteById(productoProveedorId);
        // Necesitamos redirigir al detalle del producto para el cual se desasignó el proveedor
        // Para esto, primero necesitamos obtener el productoId del ProductoProveedor que estamos eliminando.
        // Se podría mejorar pasando el productoId directamente desde el formulario HTML.
        Optional<ProductoProveedor> ppOptional = productoProveedorRepository.findById(productoProveedorId);
        if (ppOptional.isPresent()) {
            Long productoId = ppOptional.get().getProducto().getId();
            return "redirect:/productos/asignar-proveedor/" + productoId;
        }
        return "redirect:/productos"; // Redirigir a la lista de productos si no se encuentra el ProductoProveedor
    }

    @PostMapping("/desactivar/{id}")
    public String desactivarProducto(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            producto.setActivo(false); // Set to inactive
            productoService.save(producto);
        }
        return "redirect:/productos"; // Redirect back to the product list
    }

    @PostMapping("/activar/{id}")
    public String activarProducto(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            producto.setActivo(true); // Set to active
            productoService.save(producto);
        }
        return "redirect:/productos"; // Redirect back to the product list
    }
}
