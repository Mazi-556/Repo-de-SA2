package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.OrdenCompra;
import com.example.SA2Gemini.entity.Remito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemitoRepository extends JpaRepository<Remito, Long> {
    List<Remito> findAllByOrdenCompra(OrdenCompra ordenCompra);
    List<Remito> findByOrdenCompraId(Long ordenCompraId);

}
