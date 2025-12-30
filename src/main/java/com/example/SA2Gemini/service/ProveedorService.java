package com.example.SA2Gemini.service;


import org.springframework.data.domain.Sort;
import com.example.SA2Gemini.entity.Proveedor;
import com.example.SA2Gemini.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProveedorService {
    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> findAll() {
        // Ordenar por ID descendente para que el último creado aparezca primero
        return proveedorRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Proveedor findById(Long id) {
        return proveedorRepository.findById(id).orElse(null);
    }

    public Proveedor save(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }
}
