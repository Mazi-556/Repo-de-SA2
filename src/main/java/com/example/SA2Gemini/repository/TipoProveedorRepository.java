package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.TipoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoProveedorRepository extends JpaRepository<TipoProveedor, Long> {
}
