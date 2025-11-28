package com.example.SA2Gemini.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/facturas")
public class FacturaController {

    @GetMapping
    public String listarOrdenesDeCompraParaFactura(Model model) {
        // TODO: Cargar OCs en estado INGRESADA
        return "factura-listado-oc";
    }

    @GetMapping("/nuevo/{ocId}")
    public String mostrarFormularioFactura(@PathVariable Long ocId, Model model) {
        // TODO: Cargar datos de la OC seleccionada
        return "factura-form";
    }
}
