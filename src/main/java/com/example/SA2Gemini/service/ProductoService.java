package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.Producto;
import com.example.SA2Gemini.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> findAll() {
        // Sort by 'activo' status (true first, then false) and then by 'nombre'
        return productoRepository.findAll(Sort.by(Sort.Direction.DESC, "activo").and(Sort.by(Sort.Direction.ASC, "nombre")));
    }

    public Producto findById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }
}
