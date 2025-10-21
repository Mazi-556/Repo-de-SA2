package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.dto.LibroMayorCuentaReport;
import com.example.SA2Gemini.dto.LibroMayorReportData;
import com.example.SA2Gemini.entity.Asiento;
import com.example.SA2Gemini.entity.Movimiento;
import com.example.SA2Gemini.service.AsientoService;
import com.example.SA2Gemini.service.CuentaService;
import com.example.SA2Gemini.service.PdfGeneratorService;
import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReporteController {

    @Autowired
    private AsientoService asientoService;

    @Autowired
    private CuentaService cuentaService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @GetMapping("/reportes/libro-diario")
    public String showLibroDiarioForm() {
        return "libro-diario-form";
    }

    @PostMapping("/reportes/libro-diario")
    public String generateLibroDiarioReport(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                            Model model, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (startDate.isAfter(endDate)) {
            redirectAttributes.addFlashAttribute("error", "La fecha de inicio no puede ser posterior a la fecha de fin.");
            return "redirect:/reportes/libro-diario";
        }
        BigDecimal saldoInicial = asientoService.getAccumulatedDebitsUpTo(startDate);
        List<Asiento> asientos = asientoService.getAsientosBetweenDates(startDate, endDate);

        BigDecimal totalDebePeriodo = BigDecimal.ZERO;
        BigDecimal totalHaberPeriodo = BigDecimal.ZERO;

        for (Asiento asiento : asientos) {
            for (Movimiento movimiento : asiento.getMovimientos()) {
                if (movimiento.getDebe() != null) {
                    totalDebePeriodo = totalDebePeriodo.add(movimiento.getDebe());
                }
                if (movimiento.getHaber() != null) {
                    totalHaberPeriodo = totalHaberPeriodo.add(movimiento.getHaber());
                }
            }
        }

        BigDecimal saldoFinal = saldoInicial.add(totalDebePeriodo);

        model.addAttribute("asientos", asientos);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("saldoInicial", saldoInicial);
        model.addAttribute("totalDebePeriodo", totalDebePeriodo);
        model.addAttribute("totalHaberPeriodo", totalHaberPeriodo);
        model.addAttribute("saldoFinal", saldoFinal);
        return "libro-diario-report";
    }

    @GetMapping("/reportes/libro-diario/pdf")
    public ResponseEntity<byte[]> generateLibroDiarioPdf(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                         @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                         HttpServletRequest request, HttpServletResponse response) throws DocumentException {
        BigDecimal saldoInicial = asientoService.getAccumulatedDebitsUpTo(startDate);
        List<Asiento> asientos = asientoService.getAsientosBetweenDates(startDate, endDate);

        BigDecimal totalDebePeriodo = BigDecimal.ZERO;
        BigDecimal totalHaberPeriodo = BigDecimal.ZERO;

        for (Asiento asiento : asientos) {
            for (Movimiento movimiento : asiento.getMovimientos()) {
                if (movimiento.getDebe() != null) {
                    totalDebePeriodo = totalDebePeriodo.add(movimiento.getDebe());
                }
                if (movimiento.getHaber() != null) {
                    totalHaberPeriodo = totalHaberPeriodo.add(movimiento.getHaber());
                }
            }
        }

        BigDecimal saldoFinal = saldoInicial.add(totalDebePeriodo);

        Map<String, Object> data = new HashMap<>();
        data.put("asientos", asientos);
        data.put("startDate", startDate);
        data.put("endDate", endDate);
        data.put("saldoInicial", saldoInicial);
        data.put("totalDebePeriodo", totalDebePeriodo);
        data.put("totalHaberPeriodo", totalHaberPeriodo);
        data.put("saldoFinal", saldoFinal);
        data.put("isPdf", true);

        String htmlContent = pdfGeneratorService.generateHtml("libro-diario-report", data, request, response);
        byte[] pdfBytes = pdfGeneratorService.generatePdfFromHtml(htmlContent);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"libro_diario_" + startDate + "_to_" + endDate + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/reportes/libro-mayor")
    public String libroMayor(@RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                             @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                             @RequestParam(value = "cuentaId", required = false) Long cuentaId,
                             Model model) {
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now().minusDays(1);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }

        Map<String, LibroMayorCuentaReport> reportePorCuenta = asientoService.generarLibroMayorReporte(fechaInicio, fechaFin, cuentaId);

        model.addAttribute("reportePorCuenta", reportePorCuenta);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("cuentas", cuentaService.getAllCuentas());
        model.addAttribute("selectedCuentaId", cuentaId);
        return "libro-mayor-report";
    }

    @PostMapping("/reportes/libro-mayor")
    public String generateLibroMayorReport(@RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                           @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                           @RequestParam(value = "cuentaId", required = false) Long cuentaId,
                                           Model model) {

        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            model.addAttribute("error", "La fecha de inicio no puede ser posterior a la fecha de fin.");
            model.addAttribute("cuentas", cuentaService.getAllCuentas());
            model.addAttribute("selectedCuentaId", cuentaId);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
            return "libro-mayor-report";
        }

        if (fechaInicio == null) {
            fechaInicio = LocalDate.now().minusDays(1);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }

        Map<String, LibroMayorCuentaReport> reportePorCuenta = asientoService.generarLibroMayorReporte(fechaInicio, fechaFin, cuentaId);

        model.addAttribute("reportePorCuenta", reportePorCuenta);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("cuentas", cuentaService.getAllCuentas());
        model.addAttribute("selectedCuentaId", cuentaId);
        return "libro-mayor-report";
    }

    @GetMapping("/reportes/libro-mayor/pdf")
    public ResponseEntity<byte[]> generateLibroMayorPdf(@RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                                        @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                                        @RequestParam(value = "cuentaId", required = false) Long cuentaId,
                                                        HttpServletRequest request, HttpServletResponse response) throws DocumentException {
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now().minusDays(1);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }

        Map<String, LibroMayorCuentaReport> reportePorCuenta = asientoService.generarLibroMayorReporte(fechaInicio, fechaFin, cuentaId);

        Map<String, Object> data = new HashMap<>();
        data.put("reportePorCuenta", reportePorCuenta);
        data.put("fechaInicio", fechaInicio);
        data.put("fechaFin", fechaFin);
        data.put("isPdf", true);

        String htmlContent = pdfGeneratorService.generateHtml("libro-mayor-report", data, request, response);
        byte[] pdfBytes = pdfGeneratorService.generatePdfFromHtml(htmlContent);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"libro_mayor_" + fechaInicio + "_to_" + fechaFin + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}