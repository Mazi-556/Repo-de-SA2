package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.FacturaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacturaItemRepository extends JpaRepository<FacturaItem, Long> {
    List<FacturaItem> findByFacturaId(Long facturaId);
}
