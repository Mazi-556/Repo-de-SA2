package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.Asiento;
import com.example.SA2Gemini.entity.Cuenta;
import com.example.SA2Gemini.service.AsientoService;
import com.example.SA2Gemini.service.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Controller
public class AsientoController {

    @Autowired
    private AsientoService asientoService;

    @Autowired
    private CuentaService cuentaService;


    @GetMapping("/asientos/nuevo")
    public String showNewAsientoForm(Model model) {
        model.addAttribute("asiento", new Asiento());
        // Get the list of active accounts
        List<Cuenta> activeCuentas = cuentaService.getActiveCuentas();
        model.addAttribute("cuentas", activeCuentas); // Pass as List<Cuenta> for Thymeleaf's th:each

        // Convert the list to JSON string for JavaScript
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String cuentasJson = objectMapper.writeValueAsString(activeCuentas);
            System.out.println("Generated cuentasJson: " + cuentasJson); // Diagnostic print
            model.addAttribute("cuentasJson", cuentasJson); // Pass as JSON string for JavaScript
        } catch (JsonProcessingException e) {
            // Handle exception, e.g., log it or add an error message to the model
            model.addAttribute("error", "Error al cargar las cuentas: " + e.getMessage());
            model.addAttribute("cuentasJson", "[]"); // Fallback to empty array
        }
        return "asiento-form";
    }

    @PostMapping("/asientos")
    public String saveAsiento(@ModelAttribute Asiento asiento, RedirectAttributes redirectAttributes) {
        try {
            asientoService.saveAsiento(asiento);
            redirectAttributes.addFlashAttribute("success", "Asiento registrado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // It would be better to return to the form with the entered data, but for now, we redirect.
            return "redirect:/asientos/nuevo";
        }
        return "redirect:/asientos/nuevo";
    }
}
