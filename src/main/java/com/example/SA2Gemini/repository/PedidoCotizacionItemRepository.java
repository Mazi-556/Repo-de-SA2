package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.PedidoCotizacionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoCotizacionItemRepository extends JpaRepository<PedidoCotizacionItem, Long> {
}
