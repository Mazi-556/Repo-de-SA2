package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.Permiso;
import com.example.SA2Gemini.entity.Rol;
import com.example.SA2Gemini.repository.PermisoRepository;
import com.example.SA2Gemini.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    // ==================== ROLES ====================

    public List<Rol> getAllRoles() {
        return rolRepository.findAll();
    }

    public Optional<Rol> getRolById(Long id) {
        return rolRepository.findById(id);
    }

    public Optional<Rol> getRolByName(String name) {
        return rolRepository.findByName(name);
    }

    @Transactional
    public Rol saveRol(Rol rol) {
        // Validar que el nombre no esté vacío
        if (rol.getName() == null || rol.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol no puede estar vacío");
        }
        
        // Convertir a mayúsculas y sin espacios
        rol.setName(rol.getName().toUpperCase().trim().replace(" ", "_"));
        
        // Verificar si ya existe un rol con ese nombre (solo para nuevos roles)
        if (rol.getId() == null) {
            Optional<Rol> existente = rolRepository.findByName(rol.getName());
            if (existente.isPresent()) {
                throw new IllegalArgumentException("Ya existe un rol con el nombre: " + rol.getName());
            }
        }
        
        return rolRepository.save(rol);
    }

    @Transactional
    public void deleteRol(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        
        // No permitir eliminar roles del sistema
        if (!rol.isEditable()) {
            throw new IllegalArgumentException("No se puede eliminar un rol del sistema");
        }
        
        // Verificar que no haya usuarios con este rol
        // (Esto debería manejarse en el controlador o con una validación adicional)
        
        rolRepository.delete(rol);
    }

    // ==================== PERMISOS ====================

    public List<Permiso> getAllPermisos() {
        return permisoRepository.findAllByOrderByCategoriaAscNombreAsc();
    }

    public Optional<Permiso> getPermisoByCodigo(String codigo) {
        return permisoRepository.findByCodigo(codigo);
    }

    public List<String> getAllCategorias() {
        return permisoRepository.findAllCategorias();
    }

    /**
     * Obtiene todos los permisos agrupados por categoría
     */
    public Map<String, List<Permiso>> getPermisosAgrupados() {
        List<Permiso> permisos = getAllPermisos();
        return permisos.stream()
                .collect(Collectors.groupingBy(
                        Permiso::getCategoria,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    // ==================== GESTIÓN DE PERMISOS POR ROL ====================

    /**
     * Obtiene los permisos de un rol específico
     */
    public Set<Permiso> getPermisosDeRol(Long rolId) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        return rol.getPermisos();
    }

    /**
     * Actualiza los permisos de un rol
     */
    @Transactional
    public Rol actualizarPermisosDeRol(Long rolId, List<Long> permisosIds) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        
        // Limpiar permisos actuales
        rol.getPermisos().clear();
        
        // Agregar nuevos permisos
        if (permisosIds != null && !permisosIds.isEmpty()) {
            List<Permiso> nuevosPermisos = permisoRepository.findAllById(permisosIds);
            rol.getPermisos().addAll(nuevosPermisos);
        }
        
        return rolRepository.save(rol);
    }

    /**
     * Agrega un permiso a un rol
     */
    @Transactional
    public void agregarPermisoARol(Long rolId, Long permisoId) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        Permiso permiso = permisoRepository.findById(permisoId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado"));
        
        rol.addPermiso(permiso);
        rolRepository.save(rol);
    }

    /**
     * Quita un permiso de un rol
     */
    @Transactional
    public void quitarPermisoDeRol(Long rolId, Long permisoId) {
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        Permiso permiso = permisoRepository.findById(permisoId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado"));
        
        rol.removePermiso(permiso);
        rolRepository.save(rol);
    }

    /**
     * Verifica si un rol tiene un permiso específico por código
     */
    public boolean rolTienePermiso(String nombreRol, String codigoPermiso) {
        // El ADMIN siempre tiene todos los permisos
        if ("ADMIN".equals(nombreRol)) {
            return true;
        }
        
        Optional<Rol> rol = rolRepository.findByName(nombreRol);
        if (rol.isEmpty()) {
            return false;
        }
        
        return rol.get().tienePermiso(codigoPermiso);
    }

    /**
     * Verifica si un rol tiene acceso a una URL específica
     */
    public boolean rolTieneAccesoAUrl(String nombreRol, String url) {
        // El ADMIN siempre tiene acceso a todo
        if ("ADMIN".equals(nombreRol)) {
            return true;
        }
        
        Optional<Rol> rolOpt = rolRepository.findByName(nombreRol);
        if (rolOpt.isEmpty()) {
            return false;
        }
        
        Rol rol = rolOpt.get();
        for (Permiso permiso : rol.getPermisos()) {
            if (urlCoincide(url, permiso.getUrlPattern())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Verifica si una URL coincide con un patrón
     */
    private boolean urlCoincide(String url, String pattern) {
        // Convertir el patrón estilo Ant a regex
        // /cuentas/** -> coincide con /cuentas, /cuentas/, /cuentas/algo, etc.
        if (pattern.endsWith("/**")) {
            String base = pattern.substring(0, pattern.length() - 3);
            return url.equals(base) || url.startsWith(base + "/");
        }
        return url.equals(pattern);
    }
}
