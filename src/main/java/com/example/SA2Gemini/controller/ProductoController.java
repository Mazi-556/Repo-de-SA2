package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.Producto;
import com.example.SA2Gemini.repository.AlmacenRepository;
import com.example.SA2Gemini.repository.CategoriaProductoRepository;
import com.example.SA2Gemini.repository.ProveedorRepository;
import com.example.SA2Gemini.repository.ProductoProveedorRepository;
import com.example.SA2Gemini.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.SA2Gemini.entity.ProductoProveedor;
import com.example.SA2Gemini.entity.Proveedor; 
import java.util.Optional; 


@PreAuthorize("hasAnyRole('COMERCIAL', 'ADMIN')")
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
    private ProductoProveedorRepository productoProveedorRepository;

    @PreAuthorize("hasAnyRole('COMPRAS', 'ALMACEN', 'ADMIN')")
    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.findAll());
        return "productos";
    }

    @PreAuthorize("hasAnyRole('COMPRAS', 'ADMIN')")
    @GetMapping("/nuevo")
    public String mostrarFormularioDeAlta(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaProductoRepository.findAll());
        model.addAttribute("almacenes", almacenRepository.findAll());
        model.addAttribute("proveedores", proveedorRepository.findAll());
        return "producto-form";
    }

    @PreAuthorize("hasAnyRole('COMPRAS', 'ADMIN')")
    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("producto", productoService.findById(id));
        model.addAttribute("categorias", categoriaProductoRepository.findAll());
        model.addAttribute("almacenes", almacenRepository.findAll());
        return "producto-form";
    }

    @PreAuthorize("hasAnyRole('COMPRAS', 'ADMIN')")
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, Model model) {
        try {
            // Validación: el producto debe tener categoría y almacén
            if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
                model.addAttribute("error", "Debe seleccionar una categoría para el producto.");
                model.addAttribute("producto", producto);
                model.addAttribute("categorias", categoriaProductoRepository.findAll());
                model.addAttribute("almacenes", almacenRepository.findAll());
                model.addAttribute("proveedores", proveedorRepository.findAll());
                return "producto-form";
            }
            
            if (producto.getAlmacen() == null || producto.getAlmacen().getId() == null) {
                model.addAttribute("error", "Debe seleccionar un almacén para el producto.");
                model.addAttribute("producto", producto);
                model.addAttribute("categorias", categoriaProductoRepository.findAll());
                model.addAttribute("almacenes", almacenRepository.findAll());
                model.addAttribute("proveedores", proveedorRepository.findAll());
                return "producto-form";
            }
        
        // Validaciones de stock
        if (producto.getStockMinimo() < 0) {
            model.addAttribute("error", "El stock mínimo no puede ser negativo.");
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categoriaProductoRepository.findAll());
            model.addAttribute("almacenes", almacenRepository.findAll());
            model.addAttribute("proveedores", proveedorRepository.findAll());
            return "producto-form";
        }
        
        if (producto.getStockMaximo() < 0) {
            model.addAttribute("error", "El stock máximo no puede ser negativo.");
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categoriaProductoRepository.findAll());
            model.addAttribute("almacenes", almacenRepository.findAll());
            model.addAttribute("proveedores", proveedorRepository.findAll());
            return "producto-form";
        }
        
        if (producto.getStockMinimo() > 0 && producto.getStockMaximo() > 0 
            && producto.getStockMinimo() > producto.getStockMaximo()) {
            model.addAttribute("error", "El stock mínimo no puede ser mayor que el stock máximo.");
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categoriaProductoRepository.findAll());
            model.addAttribute("almacenes", almacenRepository.findAll());
            model.addAttribute("proveedores", proveedorRepository.findAll());
            return "producto-form";
        }
        
        if (producto.getPuntoReposicion() < 0) {
            model.addAttribute("error", "El punto de reposición no puede ser negativo.");
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categoriaProductoRepository.findAll());
            model.addAttribute("almacenes", almacenRepository.findAll());
            model.addAttribute("proveedores", proveedorRepository.findAll());
            return "producto-form";
        }
        
        if (producto.getPuntoReposicion() > 0 && producto.getStockMinimo() > 0 
            && producto.getPuntoReposicion() < producto.getStockMinimo()) {
            model.addAttribute("error", "El punto de reposición no puede ser menor que el stock mínimo.");
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categoriaProductoRepository.findAll());
            model.addAttribute("almacenes", almacenRepository.findAll());
            model.addAttribute("proveedores", proveedorRepository.findAll());
            return "producto-form";
        }
        
        if (producto.getPuntoReposicion() > 0 && producto.getStockMaximo() > 0 
            && producto.getPuntoReposicion() > producto.getStockMaximo()) {
            model.addAttribute("error", "El punto de reposición no puede ser mayor que el stock máximo.");
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categoriaProductoRepository.findAll());
            model.addAttribute("almacenes", almacenRepository.findAll());
            model.addAttribute("proveedores", proveedorRepository.findAll());
            return "producto-form";
        }
        
        // Validaciones de precios
        if (producto.getPrecioVenta() != null && producto.getPrecioVenta().compareTo(java.math.BigDecimal.ZERO) < 0) {
            model.addAttribute("error", "El precio de venta no puede ser negativo.");
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categoriaProductoRepository.findAll());
            model.addAttribute("almacenes", almacenRepository.findAll());
            model.addAttribute("proveedores", proveedorRepository.findAll());
            return "producto-form";
        }
        
        // Validación de IVA
        if (producto.getIva() != null && (producto.getIva().compareTo(java.math.BigDecimal.ZERO) < 0 
            || producto.getIva().compareTo(new java.math.BigDecimal("100")) > 0)) {
            model.addAttribute("error", "El IVA debe estar entre 0% y 100%.");
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categoriaProductoRepository.findAll());
            model.addAttribute("almacenes", almacenRepository.findAll());
            model.addAttribute("proveedores", proveedorRepository.findAll());
            return "producto-form";
        }
        
            // Detectamos si es un producto nuevo antes de guardarlo
            boolean esNuevo = (producto.getId() == null);
            
            // Si estamos editando un producto existente, preservar el stock actual
            if (!esNuevo) {
                Producto productoExistente = productoService.findById(producto.getId());
                if (productoExistente != null) {
                    producto.setStockActual(productoExistente.getStockActual());
                }
            }
            
            // Guardamos el producto para que la base de datos le asigne un ID
            Producto productoGuardado = productoService.save(producto);
            
            if (esNuevo) {
                // Si es nuevo, redirigimos automáticamente a la pantalla de asignar proveedores
                return "redirect:/productos/asignar-proveedor/" + productoGuardado.getId();
            } else {
                // Si solo estábamos editando, volvemos a la lista general
                return "redirect:/productos";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar el producto: " + e.getMessage());
            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categoriaProductoRepository.findAll());
            model.addAttribute("almacenes", almacenRepository.findAll());
            model.addAttribute("proveedores", proveedorRepository.findAll());
            return "producto-form";
        }
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
