package com.example.SA2Gemini.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/remitos")
public class RemitoController {

    @GetMapping
    public String listarOrdenesDeCompraParaRemito(Model model) {
        // TODO: Cargar OCs en estado COMPROMETIDA
        return "remito-listado-oc";
    }

    @GetMapping("/nuevo/{ocId}")
    public String mostrarFormularioRemito(@PathVariable Long ocId, Model model) {
        // TODO: Cargar datos de la OC seleccionada
        return "remito-form";
    }
}
