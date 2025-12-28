package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional; // Import Optional

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    List<Cuenta> findByActivoTrue();
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
    Optional<Cuenta> findByNombre(String nombre); // Added this method
    Optional<Cuenta> findByCodigo(String codigo); // Added for searching by codigo
}