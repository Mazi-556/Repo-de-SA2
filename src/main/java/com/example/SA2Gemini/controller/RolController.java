package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.Permiso;
import com.example.SA2Gemini.entity.Rol;
import com.example.SA2Gemini.service.RolService;
import com.example.SA2Gemini.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@PreAuthorize("hasRole('ADMIN')")
@Controller
@RequestMapping("/admin/roles")
public class RolController {

    @Autowired
    private RolService rolService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Lista todos los roles
     */
    @GetMapping
    public String listRoles(Model model) {
        List<Rol> roles = rolService.getAllRoles();
        model.addAttribute("roles", roles);
        return "roles-listado";
    }

    /**
     * Muestra el formulario para crear un nuevo rol
     */
    @GetMapping("/nuevo")
    public String showNewRolForm(Model model) {
        model.addAttribute("rol", new Rol());
        model.addAttribute("esNuevo", true);
        return "rol-form";
    }

    /**
     * Guarda un nuevo rol
     */
    @PostMapping
    public String saveRol(@ModelAttribute("rol") Rol rol, RedirectAttributes redirectAttributes) {
        try {
            rolService.saveRol(rol);
            redirectAttributes.addFlashAttribute("success", "Rol '" + rol.getName() + "' creado exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/roles/nuevo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear rol: " + e.getMessage());
            return "redirect:/admin/roles/nuevo";
        }
        return "redirect:/admin/roles";
    }

    /**
     * Muestra el formulario para editar un rol existente
     */
    @GetMapping("/editar/{id}")
    public String showEditRolForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return rolService.getRolById(id)
                .map(rol -> {
                    if (!rol.isEditable()) {
                        redirectAttributes.addFlashAttribute("error", "Este rol del sistema no puede ser editado.");
                        return "redirect:/admin/roles";
                    }
                    model.addAttribute("rol", rol);
                    model.addAttribute("esNuevo", false);
                    return "rol-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Rol no encontrado.");
                    return "redirect:/admin/roles";
                });
    }

    /**
     * Actualiza un rol existente
     */
    @PostMapping("/editar/{id}")
    public String updateRol(@PathVariable Long id, @ModelAttribute("rol") Rol rol, RedirectAttributes redirectAttributes) {
        try {
            Rol existingRol = rolService.getRolById(id)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            
            if (!existingRol.isEditable()) {
                redirectAttributes.addFlashAttribute("error", "Este rol del sistema no puede ser editado.");
                return "redirect:/admin/roles";
            }
            
            existingRol.setName(rol.getName());
            rolService.saveRol(existingRol);
            redirectAttributes.addFlashAttribute("success", "Rol actualizado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar rol: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    /**
     * Elimina un rol
     */
    @GetMapping("/eliminar/{id}")
    public String deleteRol(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Rol rol = rolService.getRolById(id)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            
            if (!rol.isEditable()) {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar un rol del sistema.");
                return "redirect:/admin/roles";
            }
            
            // Verificar que no haya usuarios con este rol
            long usuariosConRol = usuarioRepository.findAll().stream()
                    .filter(u -> u.getRol() != null && u.getRol().getId().equals(id))
                    .count();
            
            if (usuariosConRol > 0) {
                redirectAttributes.addFlashAttribute("error", 
                        "No se puede eliminar el rol porque hay " + usuariosConRol + " usuario(s) asignado(s) a él.");
                return "redirect:/admin/roles";
            }
            
            rolService.deleteRol(id);
            redirectAttributes.addFlashAttribute("success", "Rol eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar rol: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    /**
     * Muestra la página de gestión de permisos para un rol
     */
    @GetMapping("/{id}/permisos")
    public String showPermisosForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Rol> rolOpt = rolService.getRolById(id);
        
        if (rolOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Rol no encontrado.");
            return "redirect:/admin/roles";
        }
        
        Rol rol = rolOpt.get();
        
        // Obtener todos los permisos agrupados por categoría
        Map<String, List<Permiso>> permisosAgrupados = rolService.getPermisosAgrupados();
        
        // Obtener los IDs de permisos que tiene el rol
        Set<Long> permisosDelRol = rol.getPermisos().stream()
                .map(Permiso::getId)
                .collect(java.util.stream.Collectors.toSet());
        
        model.addAttribute("rol", rol);
        model.addAttribute("permisosAgrupados", permisosAgrupados);
        model.addAttribute("permisosDelRol", permisosDelRol);
        model.addAttribute("esAdmin", "ADMIN".equals(rol.getName()));
        
        return "rol-permisos";
    }

    /**
     * Guarda los permisos de un rol
     */
    @PostMapping("/{id}/permisos")
    public String savePermisos(@PathVariable Long id, 
                               @RequestParam(value = "permisos", required = false) List<Long> permisosIds,
                               RedirectAttributes redirectAttributes) {
        try {
            Rol rol = rolService.getRolById(id)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            
            // El rol ADMIN no se puede modificar sus permisos (tiene todos)
            if ("ADMIN".equals(rol.getName())) {
                redirectAttributes.addFlashAttribute("warning", 
                        "El rol ADMIN tiene acceso a todas las funcionalidades por defecto.");
                return "redirect:/admin/roles";
            }
            
            rolService.actualizarPermisosDeRol(id, permisosIds);
            redirectAttributes.addFlashAttribute("success", 
                    "Permisos del rol '" + rol.getName() + "' actualizados exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar permisos: " + e.getMessage());
        }
        return "redirect:/admin/roles";
    }
}
