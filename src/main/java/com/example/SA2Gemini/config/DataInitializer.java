package com.example.SA2Gemini.config;

import com.example.SA2Gemini.entity.Rol;
import com.example.SA2Gemini.entity.Rubro;
import com.example.SA2Gemini.entity.TipoProveedor;
import com.example.SA2Gemini.entity.Usuario;
import com.example.SA2Gemini.entity.Cuenta;
import com.example.SA2Gemini.repository.CuentaRepository;
import com.example.SA2Gemini.repository.RolRepository;
import com.example.SA2Gemini.repository.RubroRepository;
import com.example.SA2Gemini.repository.TipoProveedorRepository;
import com.example.SA2Gemini.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TipoProveedorRepository tipoProveedorRepository;
    private final RubroRepository rubroRepository;
    private final CuentaRepository cuentaRepository;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                           TipoProveedorRepository tipoProveedorRepository, RubroRepository rubroRepository, CuentaRepository cuentaRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tipoProveedorRepository = tipoProveedorRepository;
        this.rubroRepository = rubroRepository;
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("DataInitializer is running...");

        // Crear roles si no existen
        Rol adminRol;
        Optional<Rol> adminRolOpt = rolRepository.findByName("ADMIN");
        if (adminRolOpt.isEmpty()) {
            logger.info("Creating ADMIN role...");
            adminRol = rolRepository.save(new Rol("ADMIN"));
        } else {
            logger.info("ADMIN role already exists.");
            adminRol = adminRolOpt.get();
        }

        Rol userRol;
        Optional<Rol> userRolOpt = rolRepository.findByName("USER");
        if (userRolOpt.isEmpty()) {
            logger.info("Creating USER role...");
            userRol = rolRepository.save(new Rol("USER"));
        } else {
            logger.info("USER role already exists.");
            userRol = userRolOpt.get();
        }

        // Crear usuario normal si no existe
        if (usuarioRepository.count() == 0) {
            logger.info("No users found in the database. Creating default user...");
            Usuario user = new Usuario();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user"));
            user.setRol(userRol);
            usuarioRepository.save(user);
            logger.info("Default user 'user' created.");
        } else {
            logger.info("Users already exist in the database. Default user not created.");
        }

        // Crear tipos de proveedor si no existen
        if (tipoProveedorRepository.count() == 0) {
            logger.info("Creating default provider types...");
            tipoProveedorRepository.save(new TipoProveedor(null, "Fabricante"));
            tipoProveedorRepository.save(new TipoProveedor(null, "Distribuidor"));
            tipoProveedorRepository.save(new TipoProveedor(null, "Minorista"));
            logger.info("Default provider types created.");
        }

        // Crear rubros si no existen
        if (rubroRepository.count() == 0) {
            logger.info("Creating default rubros...");
            rubroRepository.save(new Rubro(null, "Electrónica"));
            rubroRepository.save(new Rubro(null, "Alimentos"));
            rubroRepository.save(new Rubro(null, "Servicios"));
            logger.info("Default rubros created.");
        }


        // Crear usuario administrador si no existe
        Optional<Usuario> adminUserOpt = usuarioRepository.findByUsername("admin");
        if (adminUserOpt.isEmpty()) {
            logger.info("Creating admin user...");
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin")); // Cambia la contraseña aquí
            admin.setRol(adminRol); // Se le asigna el rol ADMIN creado arriba
            usuarioRepository.save(admin);
            logger.info("Admin user 'admin' created.");
        }


        logger.info("Checking critical accounting accounts...");
        
        crearCuentaSiNoExiste("Mercaderías", com.example.SA2Gemini.entity.TipoCuenta.ACTIVO, "1.2.1");
        crearCuentaSiNoExiste("IVA Crédito Fiscal", com.example.SA2Gemini.entity.TipoCuenta.ACTIVO, "1.2.2");
        crearCuentaSiNoExiste("Proveedores", com.example.SA2Gemini.entity.TipoCuenta.PASIVO, "2.1.1");
        // Puedes agregar más si son necesarias (ej: Caja, Ventas)
        
        logger.info("Critical accounting accounts check finished.");
        
    } 
    
    // Método auxiliar para no repetir código
    private void crearCuentaSiNoExiste(String nombre, com.example.SA2Gemini.entity.TipoCuenta tipo, String codigo) {
        if (cuentaRepository.findByNombre(nombre).isEmpty()) {
            com.example.SA2Gemini.entity.Cuenta cuenta = new com.example.SA2Gemini.entity.Cuenta();
            cuenta.setNombre(nombre);
            cuenta.setTipoCuenta(tipo);
            cuenta.setCodigo(codigo); // Código ficticio por defecto
            cuenta.setActivo(true);
            cuentaRepository.save(cuenta);
            logger.info("Created account: " + nombre);
        }
    }
}


