package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.dto.LibroMayorCuentaReport;
import com.example.SA2Gemini.entity.Asiento;
import com.example.SA2Gemini.entity.Movimiento;
import com.example.SA2Gemini.service.AsientoService;
import com.example.SA2Gemini.service.CuentaService;
import com.example.SA2Gemini.service.PdfGeneratorService;
import com.example.SA2Gemini.service.ExcelGeneratorService;
import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PreAuthorize("hasAnyRole('CONTADOR', 'ADMIN')")
@Controller
public class ReporteController {

    @Autowired
    private AsientoService asientoService;

    @Autowired
    private CuentaService cuentaService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private ExcelGeneratorService excelGeneratorService;

   @GetMapping("/reportes/libro-diario")
    public String showLibroDiarioForm(Model model) {
        model.addAttribute("startDate", java.time.LocalDate.now());
        model.addAttribute("endDate", java.time.LocalDate.now());
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

        String htmlContent = pdfGeneratorService.generateHtml("libro-diario-pdf", data, request, response);
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

        String htmlContent = pdfGeneratorService.generateHtml("libro-mayor-pdf", data, request, response);
        byte[] pdfBytes = pdfGeneratorService.generatePdfFromHtml(htmlContent);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"libro_mayor_" + fechaInicio + "_to_" + fechaFin + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // ENDPOINTS EXCEL
    @GetMapping("/reportes/libro-diario/excel")
    public ResponseEntity<byte[]> generateLibroDiarioExcel(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                           @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        
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

        // Preparar datos para Excel
        List<String> headers = Arrays.asList("Fecha", "Asiento Nº", "Cuenta", "Detalle", "Debe", "Haber");
        List<List<Object>> data = new ArrayList<>();
        
        // Fila de saldo inicial
        data.add(Arrays.asList("", "", "SALDO INICIAL", "", saldoInicial, BigDecimal.ZERO));
        
        // Datos de asientos
        for (Asiento asiento : asientos) {
            for (Movimiento movimiento : asiento.getMovimientos()) {
                List<Object> row = new ArrayList<>();
                row.add(asiento.getFecha());
                row.add(asiento.getId());
                row.add(movimiento.getCuenta().getNombre());
                row.add(asiento.getDescripcion());
                row.add(movimiento.getDebe() != null ? movimiento.getDebe() : BigDecimal.ZERO);
                row.add(movimiento.getHaber() != null ? movimiento.getHaber() : BigDecimal.ZERO);
                data.add(row);
            }
        }
        
        // Fila de totales
        data.add(Arrays.asList("", "", "TOTALES", "", totalDebePeriodo, totalHaberPeriodo));
        data.add(Arrays.asList("", "", "SALDO FINAL", "", saldoFinal, BigDecimal.ZERO));

        String title = "LIBRO DIARIO - Del " + startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                      " al " + endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        byte[] excelBytes = excelGeneratorService.generateExcel("Libro Diario", headers, data, title);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"libro_diario_" + startDate + "_to_" + endDate + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/reportes/libro-mayor/excel")
    public ResponseEntity<byte[]> generateLibroMayorExcel(@RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                                          @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                                          @RequestParam(value = "cuentaId", required = false) Long cuentaId) throws IOException {
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now().minusDays(1);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }

        Map<String, LibroMayorCuentaReport> reportePorCuenta = asientoService.generarLibroMayorReporte(fechaInicio, fechaFin, cuentaId);

        // Preparar datos para Excel
        List<String> headers = Arrays.asList("Cuenta", "Fecha", "Detalle", "Debe", "Haber", "Saldo");
        List<List<Object>> data = new ArrayList<>();

        for (Map.Entry<String, LibroMayorCuentaReport> entry : reportePorCuenta.entrySet()) {
            String nombreCuenta = entry.getKey();
            LibroMayorCuentaReport cuentaReport = entry.getValue();
            
            // Saldo inicial de la cuenta
            data.add(Arrays.asList(nombreCuenta, "", "SALDO INICIAL", BigDecimal.ZERO, BigDecimal.ZERO, cuentaReport.getSaldoInicial()));
            
            // Movimientos de la cuenta
            for (com.example.SA2Gemini.dto.LibroMayorReportData movimiento : cuentaReport.getMovimientos()) {
                List<Object> row = new ArrayList<>();
                row.add(""); // Cuenta vacía para las filas de movimiento
                row.add(movimiento.getFecha());
                row.add(movimiento.getDetalle());
                row.add(movimiento.getDebe() != null ? movimiento.getDebe() : BigDecimal.ZERO);
                row.add(movimiento.getHaber() != null ? movimiento.getHaber() : BigDecimal.ZERO);
                row.add(movimiento.getSaldo());
                data.add(row);
            }
            
            // Saldo final de la cuenta
            data.add(Arrays.asList("", "", "SALDO FINAL", BigDecimal.ZERO, BigDecimal.ZERO, cuentaReport.getSaldoFinal()));
            
            // Línea en blanco entre cuentas
            data.add(Arrays.asList("", "", "", "", "", ""));
        }

        String title = "LIBRO MAYOR - Del " + fechaInicio.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                      " al " + fechaFin.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        byte[] excelBytes = excelGeneratorService.generateExcel("Libro Mayor", headers, data, title);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"libro_mayor_" + fechaInicio + "_to_" + fechaFin + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}