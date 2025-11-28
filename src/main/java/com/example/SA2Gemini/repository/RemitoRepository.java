package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.OrdenCompra;
import com.example.SA2Gemini.entity.Remito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RemitoRepository extends JpaRepository<Remito, Long> {
    Optional<Remito> findByOrdenCompra(OrdenCompra ordenCompra);
}
