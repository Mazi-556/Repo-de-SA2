package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.Cuenta;
import com.example.SA2Gemini.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByCuenta(Cuenta cuenta);
    List<Movimiento> findByCuentaAndAsiento_FechaBetween(Cuenta cuenta, LocalDate startDate, LocalDate endDate);

    @Query("SELECT m FROM Movimiento m WHERE m.cuenta = :cuenta AND m.asiento.fecha < :date ORDER BY m.asiento.fecha ASC, m.id ASC")
    List<Movimiento> findByCuentaAndAsiento_FechaBeforeOrderByFechaAscIdAsc(@Param("cuenta") Cuenta cuenta, @Param("date") LocalDate date);

    @Query("SELECT SUM(m.debe) FROM Movimiento m WHERE m.asiento.fecha < :date")
    BigDecimal sumDebeUpToDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(m.haber) FROM Movimiento m WHERE m.asiento.fecha < :date")
    BigDecimal sumHaberUpToDate(@Param("date") LocalDate date);
}
