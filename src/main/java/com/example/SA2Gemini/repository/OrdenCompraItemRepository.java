package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.OrdenCompraItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenCompraItemRepository extends JpaRepository<OrdenCompraItem, Long> {
}
