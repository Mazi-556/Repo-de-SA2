package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.SolicitudCompra;
import com.example.SA2Gemini.entity.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SolicitudCompraRepository extends JpaRepository<SolicitudCompra, Long> {
    List<SolicitudCompra> findByEstado(EstadoSolicitud estado);
}
