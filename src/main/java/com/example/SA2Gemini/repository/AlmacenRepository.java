package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {
}