package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.PedidoCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoCotizacionRepository extends JpaRepository<PedidoCotizacion, Long> {
}
