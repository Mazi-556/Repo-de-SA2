package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.Asiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Long> {
    List<Asiento> findByFechaBetween(LocalDate startDate, LocalDate endDate);
    Optional<Asiento> findTopByOrderByFechaDesc();
}
