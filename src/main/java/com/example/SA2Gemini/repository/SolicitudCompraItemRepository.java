package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.SolicitudCompraItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudCompraItemRepository extends JpaRepository<SolicitudCompraItem, Long> {
    List<SolicitudCompraItem> findByIdIn(List<Long> ids);
}
