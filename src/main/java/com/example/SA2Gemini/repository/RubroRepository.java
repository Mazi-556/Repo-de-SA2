package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.Rubro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RubroRepository extends JpaRepository<Rubro, Long> {
}
