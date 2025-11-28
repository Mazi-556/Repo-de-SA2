package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.ProductoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, Long> {
}
