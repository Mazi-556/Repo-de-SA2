package com.example.SA2Gemini.config;

import com.example.SA2Gemini.entity.Rol;
import com.example.SA2Gemini.entity.Usuario;
import com.example.SA2Gemini.repository.RolRepository;
import com.example.SA2Gemini.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Crear roles si no existen
        Rol adminRol = rolRepository.findByName("ADMIN").orElseGet(() -> rolRepository.save(new Rol("ADMIN")));
        Rol userRol = rolRepository.findByName("USER").orElseGet(() -> rolRepository.save(new Rol("USER")));

        // Crear usuario administrador si no existe
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRol(adminRol);
            usuarioRepository.save(admin);
        }

        // Crear usuario normal si no existe
        if (usuarioRepository.findByUsername("user").isEmpty()) {
            Usuario user = new Usuario();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user"));
            user.setRol(userRol);
            usuarioRepository.save(user);
        }
    }
}
