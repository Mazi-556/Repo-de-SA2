package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByName(String name);
}
