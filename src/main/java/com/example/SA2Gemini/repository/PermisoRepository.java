package com.example.SA2Gemini.repository;

import com.example.SA2Gemini.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    
    Optional<Permiso> findByCodigo(String codigo);
    
    List<Permiso> findByCategoria(String categoria);
    
    @Query("SELECT DISTINCT p.categoria FROM Permiso p ORDER BY p.categoria")
    List<String> findAllCategorias();
    
    @Query("SELECT p FROM Permiso p JOIN p.roles r WHERE r.id = :rolId")
    List<Permiso> findByRolId(@Param("rolId") Long rolId);
    
    List<Permiso> findAllByOrderByCategoriaAscNombreAsc();
}
