package com.example.SA2Gemini.controller;

import com.example.SA2Gemini.entity.AuditLog;
import com.example.SA2Gemini.repository.AuditLogRepository;
import com.example.SA2Gemini.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/auditoria")
@PreAuthorize("hasAnyRole('CONTADOR', 'ADMIN')")
public class AuditoriaController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping
    public String mostrarAuditoria(Model model) {
        // Obtener logs recientes
        List<AuditLog> logsRecientes = auditService.getRecentAuditLogs(50);
        
        // Estadísticas
        List<Object[]> actividadPorUsuario = auditLogRepository.getUserActivityStats();
        List<Object[]> actividadPorEntidad = auditLogRepository.getEntityActivityStats();
        
        model.addAttribute("logsRecientes", logsRecientes);
        model.addAttribute("actividadPorUsuario", actividadPorUsuario);
        model.addAttribute("actividadPorEntidad", actividadPorEntidad);
        
        return "auditoria-dashboard";
    }

    @GetMapping("/buscar")
    public String buscarAuditoria(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String entidadId,
            @RequestParam(required = false) AuditLog.OperationType operacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        List<AuditLog> resultados;

        if (entidad != null && entidadId != null && !entidad.trim().isEmpty() && !entidadId.trim().isEmpty()) {
            // Buscar por entidad específica
            resultados = auditService.getAuditLogsForEntity(entidad, entidadId);
        } else if (usuario != null && !usuario.trim().isEmpty()) {
            if (fechaInicio != null && fechaFin != null) {
                resultados = auditLogRepository.findByUserNameAndTimestampBetweenOrderByTimestampDesc(
                    usuario, fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
            } else {
                resultados = auditService.getAuditLogsByUser(usuario);
            }
        } else if (entidad != null && !entidad.trim().isEmpty()) {
            if (fechaInicio != null && fechaFin != null) {
                resultados = auditLogRepository.findByEntityNameAndTimestampBetweenOrderByTimestampDesc(
                    entidad, fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
            } else {
                resultados = auditLogRepository.findByEntityNameOrderByTimestampDesc(entidad);
            }
        } else if (operacion != null) {
            resultados = auditLogRepository.findByOperationTypeOrderByTimestampDesc(operacion);
        } else if (fechaInicio != null && fechaFin != null) {
            resultados = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
                fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59));
        } else {
            resultados = auditService.getRecentAuditLogs(100);
        }

        model.addAttribute("resultados", resultados);
        model.addAttribute("usuario", usuario);
        model.addAttribute("entidad", entidad);
        model.addAttribute("entidadId", entidadId);
        model.addAttribute("operacion", operacion);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("operaciones", AuditLog.OperationType.values());

        return "auditoria-buscar";
    }

    @GetMapping("/entidad")
    public String verAuditoriaEntidad(
            @RequestParam String entidad,
            @RequestParam String entidadId,
            Model model) {

        List<AuditLog> logs = auditService.getAuditLogsForEntity(entidad, entidadId);
        
        model.addAttribute("logs", logs);
        model.addAttribute("entidad", entidad);
        model.addAttribute("entidadId", entidadId);

        return "auditoria-entidad";
    }
}