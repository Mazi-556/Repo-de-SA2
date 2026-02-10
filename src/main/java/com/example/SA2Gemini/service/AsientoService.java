package com.example.SA2Gemini.service;

import com.example.SA2Gemini.dto.LibroMayorCuentaReport;
import com.example.SA2Gemini.dto.LibroMayorReportData;
import com.example.SA2Gemini.entity.*;
import com.example.SA2Gemini.repository.AsientoRepository;
import com.example.SA2Gemini.repository.CuentaRepository;
import com.example.SA2Gemini.repository.MovimientoRepository;
import com.example.SA2Gemini.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class AsientoService {

    @Autowired
    private AsientoRepository asientoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public void saveAsiento(Asiento asiento) throws Exception {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new Exception("Usuario no autenticado.");
        }
        String username = authentication.getName();
        Usuario currentUser = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("Usuario autenticado no encontrado en la base de datos."));

        asiento.setUsuarioCreador(currentUser);

        if (asiento.getMovimientos() == null || asiento.getMovimientos().isEmpty()) {
            throw new Exception("Un asiento debe tener al menos un movimiento.");
        }

        if (asiento.getMovimientos().size() < 2) {
            throw new Exception("No se pueden agregar asientos que tengan solo un movimiento.");
        }

        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;

        // Chronology Rule: New asiento date must be equal to or later than the last asiento date.
        asientoRepository.findTopByOrderByFechaDesc().ifPresent(lastAsiento -> {
            if (asiento.getFecha().isBefore(lastAsiento.getFecha())) {
                throw new RuntimeException("La fecha del asiento no puede ser anterior a la del último asiento registrado (" + lastAsiento.getFecha() + ").");
            }
        });

        // Future Date Rule: Asiento date cannot be in the future.
        if (asiento.getFecha().isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha del asiento no puede ser una fecha futura.");
        }

        Set<Long> debitAccountIds = new HashSet<>();
        Set<Long> creditAccountIds = new HashSet<>();

        for (Movimiento movimiento : asiento.getMovimientos()) {
            if (movimiento.getCuenta() == null || movimiento.getCuenta().getId() == null) {
                throw new Exception("Todos los movimientos deben tener una cuenta asociada.");
            }

            boolean hasDebe = movimiento.getDebe() != null && movimiento.getDebe().compareTo(BigDecimal.ZERO) > 0;
            boolean hasHaber = movimiento.getHaber() != null && movimiento.getHaber().compareTo(BigDecimal.ZERO) > 0;

            if (!hasDebe && !hasHaber) {
                throw new Exception("Cada movimiento debe tener un monto en el Debe o en el Haber.");
            }
            if (hasDebe && hasHaber) {
                throw new Exception("Un movimiento no puede tener monto en el Debe y en el Haber simultáneamente.");
            }

            if (hasDebe) {
                totalDebe = totalDebe.add(movimiento.getDebe());
                debitAccountIds.add(movimiento.getCuenta().getId());
            } else {
                totalHaber = totalHaber.add(movimiento.getHaber());
                creditAccountIds.add(movimiento.getCuenta().getId());
            }
        }

        // Check for accounts that are both debited and credited in the same asiento
        for (Long debitAccountId : debitAccountIds) {
            if (creditAccountIds.contains(debitAccountId)) {
                throw new Exception("Una cuenta no puede ser debitada y acreditada en el mismo asiento.");
            }
        }

        if (totalDebe.compareTo(totalHaber) != 0) {
            throw new Exception("El total del debe no coincide con el total del haber.");
        }

        if (totalDebe.compareTo(BigDecimal.ZERO) == 0 && totalHaber.compareTo(BigDecimal.ZERO) == 0) {
            throw new Exception("El asiento no puede tener montos totales en cero.");
        }

        // Todas las cuentas pueden tener saldo negativo - validación removida

        // Set the back-reference from Movimiento to Asiento
        for (Movimiento movimiento : asiento.getMovimientos()) {
            movimiento.setAsiento(asiento);
        }

        asientoRepository.save(asiento);
    }

    public List<Asiento> getAsientosBetweenDates(LocalDate startDate, LocalDate endDate) {
        return asientoRepository.findByFechaBetween(startDate, endDate);
    }


    public BigDecimal calculateTotalBalanceUpTo(LocalDate date) {
        BigDecimal totalDebe = movimientoRepository.sumDebeUpToDate(date);
        BigDecimal totalHaber = movimientoRepository.sumHaberUpToDate(date);
        return (totalDebe != null ? totalDebe : BigDecimal.ZERO).subtract(totalHaber != null ? totalHaber : BigDecimal.ZERO);
    }

    public BigDecimal getAccountBalanceUpToDate(Cuenta cuenta, LocalDate date) {
        List<Movimiento> movementsBeforeDate = movimientoRepository.findByCuentaAndAsiento_FechaBeforeOrderByFechaAscIdAsc(cuenta, date);
        BigDecimal balance = BigDecimal.ZERO;
        TipoCuenta tipoCuenta = cuenta.getTipoCuenta();

        for (Movimiento mov : movementsBeforeDate) {
            BigDecimal debe = mov.getDebe() != null ? mov.getDebe() : BigDecimal.ZERO;
            BigDecimal haber = mov.getHaber() != null ? mov.getHaber() : BigDecimal.ZERO;

            if (tipoCuenta == null) { // Default to ACTIVO logic
                balance = balance.add(debe).subtract(haber);
            } else {
                switch (tipoCuenta) {
                    case ACTIVO:
                    case RESULTADO_NEGATIVO:
                        balance = balance.add(debe).subtract(haber);
                        break;
                    case PASIVO:
                    case PATRIMONIO:
                    case RESULTADO_POSITIVO:
                        balance = balance.subtract(debe).add(haber);
                        break;
                }
            }
        }
        return balance;
    }

    public BigDecimal getAccumulatedDebitsUpTo(LocalDate date) {
        BigDecimal totalDebe = movimientoRepository.sumDebeUpToDate(date);
        return totalDebe != null ? totalDebe : BigDecimal.ZERO;
    }

    public Map<String, LibroMayorCuentaReport> generarLibroMayorReporte(LocalDate fechaInicio, LocalDate fechaFin, Long cuentaId) {
        Map<String, LibroMayorCuentaReport> reportePorCuenta = new LinkedHashMap<>();

        List<Cuenta> cuentas;
        if (cuentaId != null) {
            cuentas = cuentaRepository.findById(cuentaId)
                    .filter(Cuenta::isActivo)
                    .map(List::of)
                    .orElse(new ArrayList<>());
        } else {
            cuentas = cuentaRepository.findByActivoTrue();
        }

        for (Cuenta cuenta : cuentas) {
            LibroMayorCuentaReport cuentaReport = new LibroMayorCuentaReport();
            List<LibroMayorReportData> movimientosCuenta = new ArrayList<>();

            BigDecimal saldoInicial = getAccountBalanceUpToDate(cuenta, fechaInicio);
            cuentaReport.setSaldoInicial(saldoInicial);

            BigDecimal saldoActual = saldoInicial;

            List<Movimiento> movimientos = movimientoRepository.findByCuentaAndAsiento_FechaBetween(cuenta, fechaInicio, fechaFin);
            movimientos.sort(Comparator
                    .comparing((Movimiento mov) -> mov.getAsiento().getFecha())
                    .thenComparing(mov -> mov.getAsiento().getId()));

            for (Movimiento movimiento : movimientos) {
                LibroMayorReportData data = new LibroMayorReportData();
                data.setFecha(movimiento.getAsiento().getFecha());
                data.setDetalle(movimiento.getAsiento().getDescripcion());
                data.setDebe(movimiento.getDebe());
                data.setHaber(movimiento.getHaber());

                TipoCuenta tipoCuenta = cuenta.getTipoCuenta();
                // Use a safer way to add/subtract to avoid NullPointerException
                BigDecimal debe = movimiento.getDebe() != null ? movimiento.getDebe() : BigDecimal.ZERO;
                BigDecimal haber = movimiento.getHaber() != null ? movimiento.getHaber() : BigDecimal.ZERO;

                if (tipoCuenta == null) {
                    saldoActual = saldoActual.add(debe).subtract(haber);
                } else {
                    switch (tipoCuenta) {
                        case ACTIVO:
                        case RESULTADO_NEGATIVO:
                            saldoActual = saldoActual.add(debe).subtract(haber);
                            break;
                        case PASIVO:
                        case PATRIMONIO:
                        case RESULTADO_POSITIVO:
                            saldoActual = saldoActual.subtract(debe).add(haber);
                            break;
                    }
                }
                data.setSaldo(saldoActual);
                movimientosCuenta.add(data);
            }

            cuentaReport.setMovimientos(movimientosCuenta);
            cuentaReport.setSaldoFinal(saldoActual);

            // Only add the account to the report if it has an initial balance or movements in the period
            if (!movimientosCuenta.isEmpty() || saldoInicial.compareTo(BigDecimal.ZERO) != 0) {
                reportePorCuenta.put(cuenta.getNombre(), cuentaReport);
            }
        }

        return reportePorCuenta;
    }
}