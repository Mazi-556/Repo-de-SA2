package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.Rol;
import com.example.SA2Gemini.entity.Usuario;
import com.example.SA2Gemini.repository.RolRepository;
import com.example.SA2Gemini.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario saveUsuario(Usuario usuario) {
        // Encode password if it's a new user or password is changed
        if (usuario.getId() == null || (usuario.getPassword() != null && !usuario.getPassword().isEmpty())) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    public List<Rol> getAllRoles() {
        return rolRepository.findAll();
    }

    public Optional<Rol> getRolById(Long id) {
        return rolRepository.findById(id);
    }
}
