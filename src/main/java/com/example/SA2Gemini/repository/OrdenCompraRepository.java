package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
}