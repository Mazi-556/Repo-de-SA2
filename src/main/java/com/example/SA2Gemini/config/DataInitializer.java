package com.example.SA2Gemini.config;

import com.example.SA2Gemini.entity.Rol;
import com.example.SA2Gemini.entity.Rubro;
import com.example.SA2Gemini.entity.TipoProveedor;
import com.example.SA2Gemini.entity.Usuario;
import com.example.SA2Gemini.repository.RolRepository;
import com.example.SA2Gemini.repository.RubroRepository;
import com.example.SA2Gemini.repository.TipoProveedorRepository;
import com.example.SA2Gemini.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TipoProveedorRepository tipoProveedorRepository;
    private final RubroRepository rubroRepository;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                           TipoProveedorRepository tipoProveedorRepository, RubroRepository rubroRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tipoProveedorRepository = tipoProveedorRepository;
        this.rubroRepository = rubroRepository;
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

        // Crear tipos de proveedor si no existen
        if (tipoProveedorRepository.count() == 0) {
            tipoProveedorRepository.save(new TipoProveedor(null, "Fabricante"));
            tipoProveedorRepository.save(new TipoProveedor(null, "Distribuidor"));
            tipoProveedorRepository.save(new TipoProveedor(null, "Minorista"));
        }

        // Crear rubros si no existen
        if (rubroRepository.count() == 0) {
            rubroRepository.save(new Rubro(null, "Electrónica"));
            rubroRepository.save(new Rubro(null, "Alimentos"));
            rubroRepository.save(new Rubro(null, "Servicios"));
        }
    }
}
