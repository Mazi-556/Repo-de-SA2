package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
}
