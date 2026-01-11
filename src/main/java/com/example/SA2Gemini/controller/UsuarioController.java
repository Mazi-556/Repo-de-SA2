package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.Usuario;
import com.example.SA2Gemini.repository.RolRepository;
import com.example.SA2Gemini.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@PreAuthorize("hasRole('ADMIN')")
@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    private RolRepository rolRepository;

    @GetMapping
    public String listUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.getAllUsuarios());
        return "usuarios";
    }

    @GetMapping("/nuevo")
    public String showNewUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", usuarioService.getAllRoles());
        return "usuario-form";
    }

    @PostMapping
    public String saveUsuario(@ModelAttribute("usuario") Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.saveUsuario(usuario);
            redirectAttributes.addFlashAttribute("success", "Usuario guardado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String showEditUsuarioForm(@PathVariable Long id, Model model) {
        usuarioService.getUsuarioById(id).ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
            model.addAttribute("roles", usuarioService.getAllRoles());
        });
        return "usuario-form";
    }

    @PostMapping("/editar/{id}")
    public String updateUsuario(@PathVariable Long id, @ModelAttribute("usuario") Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            // Fetch existing user to preserve password if not changed
            Usuario existingUser = usuarioService.getUsuarioById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

            existingUser.setUsername(usuario.getUsername());
            existingUser.setRol(usuario.getRol());

            // Only update password if a new one is provided
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                existingUser.setPassword(usuario.getPassword());
            }

            usuarioService.saveUsuario(existingUser);
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/eliminar/{id}")
    public String deleteUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.deleteUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
