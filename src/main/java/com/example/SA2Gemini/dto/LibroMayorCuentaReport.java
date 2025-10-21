package com.example.SA2Gemini.dto;

import java.math.BigDecimal;
import java.util.List;

public class LibroMayorCuentaReport {

    private BigDecimal saldoInicial;
    private List<LibroMayorReportData> movimientos;
    private BigDecimal saldoFinal;

    // Getters and Setters

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public List<LibroMayorReportData> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<LibroMayorReportData> movimientos) {
        this.movimientos = movimientos;
    }

    public BigDecimal getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(BigDecimal saldoFinal) {
        this.saldoFinal = saldoFinal;
    }
}
