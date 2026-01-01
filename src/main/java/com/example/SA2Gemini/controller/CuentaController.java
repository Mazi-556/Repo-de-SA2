package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.Cuenta;
import com.example.SA2Gemini.entity.TipoCuenta;
import com.example.SA2Gemini.service.CodigoCuentaExistsException;
import com.example.SA2Gemini.service.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CuentaController {

    @Autowired
    private CuentaService cuentaService;

    @GetMapping("/cuentas")
    public String listCuentas(Model model) {
        model.addAttribute("cuentas", cuentaService.getAllCuentas());
        return "cuentas";
    }

    @GetMapping("/admin/cuentas/nuevo")
    public String showNewCuentaForm(Model model) {
        model.addAttribute("cuenta", new Cuenta());
        model.addAttribute("tiposCuenta", TipoCuenta.values());
        model.addAttribute("cuentas", cuentaService.getAllCuentas());
        return "cuenta-form";
    }

    @PostMapping("/admin/cuentas")
    public String saveCuenta(@ModelAttribute("cuenta") Cuenta cuenta, Model model) {
        try {
            cuentaService.saveCuenta(cuenta);
            return "redirect:/cuentas";
        } catch (CodigoCuentaExistsException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tiposCuenta", TipoCuenta.values());
            return "cuenta-form";
        }
    }

    @GetMapping("/admin/cuentas/editar/{id}")
    public String showEditCuentaForm(@PathVariable Long id, Model model) {
        model.addAttribute("cuenta", cuentaService.getCuentaById(id));
        model.addAttribute("tiposCuenta", TipoCuenta.values());
        model.addAttribute("cuentas", cuentaService.getAllCuentas());
        return "cuenta-form";
    }

    @PostMapping("/admin/cuentas/editar/{id}")
    public String updateCuenta(@PathVariable Long id, @ModelAttribute("cuenta") Cuenta cuenta, Model model) {
        Cuenta existingCuenta = cuentaService.getCuentaById(id);
        if (existingCuenta != null) {
            // Modification restriction: do not allow changing the code if the account is in use.
            if (cuentaService.isCuentaInUse(id)) {
                existingCuenta.setNombre(cuenta.getNombre()); // Only allow name change
                existingCuenta.setTipoCuenta(cuenta.getTipoCuenta()); // Allow tipoCuenta change
            } else {
                existingCuenta.setCodigo(cuenta.getCodigo());
                existingCuenta.setNombre(cuenta.getNombre());
                existingCuenta.setTipoCuenta(cuenta.getTipoCuenta()); // Allow tipoCuenta change
            }
            // Allow changing the parent account
            existingCuenta.setCuentaPadreId(cuenta.getCuentaPadreId());
            try {
                cuentaService.saveCuenta(existingCuenta);
            } catch (CodigoCuentaExistsException e) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("tiposCuenta", TipoCuenta.values());
                return "cuenta-form";
            }
        }
        return "redirect:/cuentas";
    }

    @GetMapping("/admin/cuentas/eliminar/{id}")
    public String deleteCuenta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!cuentaService.deleteCuenta(id)) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar la cuenta porque ya ha sido utilizada en asientos.");
        }
        return "redirect:/cuentas";
    }

    @PostMapping("/admin/cuentas/toggleActivo/{id}")
    public String toggleCuentaActivoStatus(@PathVariable Long id) {
        cuentaService.toggleCuentaActivoStatus(id);
        return "redirect:/cuentas";
    }
}