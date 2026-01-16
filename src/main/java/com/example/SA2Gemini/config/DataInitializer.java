package com.example.SA2Gemini.config;

import com.example.SA2Gemini.entity.Rol;
import com.example.SA2Gemini.entity.Rubro;
import com.example.SA2Gemini.entity.TipoProveedor;
import com.example.SA2Gemini.entity.Usuario;
import com.example.SA2Gemini.entity.Cuenta;
import com.example.SA2Gemini.entity.Permiso;
import com.example.SA2Gemini.repository.CuentaRepository;
import com.example.SA2Gemini.repository.RolRepository;
import com.example.SA2Gemini.repository.RubroRepository;
import com.example.SA2Gemini.repository.TipoProveedorRepository;
import com.example.SA2Gemini.repository.UsuarioRepository;
import com.example.SA2Gemini.repository.PermisoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
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
    private final JdbcTemplate jdbcTemplate;
    private final PermisoRepository permisoRepository;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                           TipoProveedorRepository tipoProveedorRepository, RubroRepository rubroRepository, 
                           CuentaRepository cuentaRepository, JdbcTemplate jdbcTemplate,
                           PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tipoProveedorRepository = tipoProveedorRepository;
        this.rubroRepository = rubroRepository;
        this.cuentaRepository = cuentaRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.permisoRepository = permisoRepository;
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

        // Crear los nuevos roles: CONTADOR, DEPOSITO, COMERCIAL
        Rol contadorRol = crearRolSiNoExiste("CONTADOR");
        Rol depositoRol = crearRolSiNoExiste("DEPOSITO");
        Rol comercialRol = crearRolSiNoExiste("COMERCIAL");

        // Migrar usuarios de roles viejos a roles nuevos y eliminar roles obsoletos
        migrarYEliminarRolesViejos(adminRol, contadorRol, depositoRol, comercialRol);

        // Crear usuarios por defecto si no existen
        crearUsuarioSiNoExiste("contador1", "contador1", contadorRol);
        crearUsuarioSiNoExiste("deposito1", "deposito1", depositoRol);
        crearUsuarioSiNoExiste("comercial1", "comercial1", comercialRol);

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

        // Los roles nuevos ya se crean arriba: CONTADOR, DEPOSITO, COMERCIAL


        logger.info("Checking critical accounting accounts...");
        
        crearCuentaSiNoExiste("Mercaderías", com.example.SA2Gemini.entity.TipoCuenta.ACTIVO, "1.2.1");
        crearCuentaSiNoExiste("IVA Crédito Fiscal", com.example.SA2Gemini.entity.TipoCuenta.ACTIVO, "1.2.2");
        crearCuentaSiNoExiste("Proveedores", com.example.SA2Gemini.entity.TipoCuenta.PASIVO, "2.1.1");
        crearCuentaSiNoExiste("Caja", com.example.SA2Gemini.entity.TipoCuenta.ACTIVO, "1.1.1");
        crearCuentaSiNoExiste("Banco", com.example.SA2Gemini.entity.TipoCuenta.ACTIVO, "1.1.2");
        crearCuentaSiNoExiste("Valores a depositar", com.example.SA2Gemini.entity.TipoCuenta.ACTIVO, "1.1.3");
        crearCuentaSiNoExiste("Ventas", com.example.SA2Gemini.entity.TipoCuenta.RESULTADO_POSITIVO, "4.1.1");
        crearCuentaSiNoExiste("Costo de Mercaderías Vendidas", com.example.SA2Gemini.entity.TipoCuenta.RESULTADO_NEGATIVO, "5.1.1");
        
        logger.info("Critical accounting accounts check finished.");
        
        // Migrar estados antiguos de SolicitudCompra
        migrarEstadosSolicitudCompra();
        
        // Inicializar permisos del sistema
        inicializarPermisos();
        
        // Asignar permisos por defecto a roles
        asignarPermisosARoles(contadorRol, depositoRol, comercialRol);
        
    }
    
    private void migrarEstadosSolicitudCompra() {
        logger.info("Migrando estados antiguos de SolicitudCompra...");
        try {
            // Primero, eliminar el constraint check que limita los valores del enum
            try {
                jdbcTemplate.execute("ALTER TABLE solicitud_compra DROP CONSTRAINT IF EXISTS solicitud_compra_estado_check");
                logger.info("Constraint check eliminado (si existía)");
            } catch (Exception e) {
                logger.info("No se pudo eliminar constraint (puede que no exista): " + e.getMessage());
            }
            
            // Ahora migrar los datos
            int updatedInicio = jdbcTemplate.update(
                "UPDATE solicitud_compra SET estado = 'PENDIENTE' WHERE estado = 'INICIO'");
            
            int updatedPresupuestada = jdbcTemplate.update(
                "UPDATE solicitud_compra SET estado = 'COTIZANDO' WHERE estado = 'PRESUPUESTADA'");
            
            logger.info("Migrados " + updatedInicio + " registros de INICIO a PENDIENTE");
            logger.info("Migrados " + updatedPresupuestada + " registros de PRESUPUESTADA a COTIZANDO");
        } catch (Exception e) {
            logger.warn("Error al migrar estados: " + e.getMessage());
            logger.info("Posiblemente ya fueron migrados previamente.");
        }
    } 
    
    // Método auxiliar para no repetir código
    private void crearCuentaSiNoExiste(String nombre, com.example.SA2Gemini.entity.TipoCuenta tipo, String codigo) {
        try {
            if (cuentaRepository.findByNombre(nombre).isEmpty()) {
                com.example.SA2Gemini.entity.Cuenta cuenta = new com.example.SA2Gemini.entity.Cuenta();
                cuenta.setNombre(nombre);
                cuenta.setTipoCuenta(tipo);
                cuenta.setCodigo(codigo); // Código ficticio por defecto
                cuenta.setActivo(true);
                cuentaRepository.save(cuenta);
                logger.info("Created account: " + nombre);
            }
        } catch (Exception e) {
            // Ignorar si ya existe (puede ser por duplicado de código)
            logger.debug("Account " + nombre + " already exists or error occurred: " + e.getMessage());
        }
    }

    private Rol crearRolSiNoExiste(String nombreRol) {
        Optional<Rol> rolOpt = rolRepository.findByName(nombreRol);
        if (rolOpt.isEmpty()) {
            logger.info("Creando rol: " + nombreRol);
            return rolRepository.save(new Rol(nombreRol));
        }
        return rolOpt.get();
    }

    private void crearUsuarioSiNoExiste(String username, String password, Rol rol) {
        if (usuarioRepository.findByUsername(username).isEmpty()) {
            logger.info("Creando usuario: " + username);
            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setRol(rol);
            usuarioRepository.save(usuario);
            logger.info("Usuario '" + username + "' creado con rol " + rol.getName());
        }
    }

    private void migrarYEliminarRolesViejos(Rol adminRol, Rol contadorRol, Rol depositoRol, Rol comercialRol) {
        logger.info("Migrando usuarios de roles viejos a roles nuevos...");
        
        // Mapeo de roles viejos a nuevos:
        // COMPRAS -> COMERCIAL
        // CONTABILIDAD -> CONTADOR
        // ALMACEN -> DEPOSITO
        // USER -> COMERCIAL (por defecto)
        
        String[][] migraciones = {
            {"COMPRA_VENTA", "COMERCIAL"},
            {"CONTABILIDAD", "CONTADOR"},
            {"ALMACEN", "DEPOSITO"},
            {"USER", "COMERCIAL"}
        };
        
        for (String[] migracion : migraciones) {
            String rolViejo = migracion[0];
            String rolNuevo = migracion[1];
            
            Optional<Rol> rolViejoOpt = rolRepository.findByName(rolViejo);
            if (rolViejoOpt.isPresent()) {
                Rol rolAntiguo = rolViejoOpt.get();
                Rol rolDestino;
                
                switch (rolNuevo) {
                    case "CONTADOR":
                        rolDestino = contadorRol;
                        break;
                    case "DEPOSITO":
                        rolDestino = depositoRol;
                        break;
                    case "COMERCIAL":
                        rolDestino = comercialRol;
                        break;
                    default:
                        rolDestino = adminRol;
                }
                
                // Migrar usuarios con el rol viejo al rol nuevo
                var usuariosConRolViejo = usuarioRepository.findAll().stream()
                    .filter(u -> u.getRol() != null && u.getRol().getId().equals(rolAntiguo.getId()))
                    .toList();
                
                for (Usuario usuario : usuariosConRolViejo) {
                    logger.info("Migrando usuario '{}' de rol {} a {}", usuario.getUsername(), rolViejo, rolNuevo);
                    usuario.setRol(rolDestino);
                    usuarioRepository.save(usuario);
                }
                
                // Eliminar el rol viejo
                try {
                    rolRepository.delete(rolAntiguo);
                    logger.info("Rol {} eliminado exitosamente", rolViejo);
                } catch (Exception e) {
                    logger.warn("No se pudo eliminar el rol {}: {}", rolViejo, e.getMessage());
                }
            }
        }
        
        logger.info("Migración de roles completada.");
    }

    private void inicializarPermisos() {
        logger.info("Inicializando permisos del sistema...");
        
        // Categoría: Contabilidad
        crearPermisoSiNoExiste("CUENTAS", "Plan de Cuentas", "/cuentas/**", 
                "Gestión del plan de cuentas contable", "Contabilidad");
        crearPermisoSiNoExiste("ASIENTOS", "Registrar Asientos", "/asientos/**", 
                "Registro de asientos contables", "Contabilidad");
        crearPermisoSiNoExiste("LIBRO_DIARIO", "Libro Diario", "/reportes/libro-diario/**", 
                "Consulta del libro diario", "Contabilidad");
        crearPermisoSiNoExiste("LIBRO_MAYOR", "Libro Mayor", "/reportes/libro-mayor/**", 
                "Consulta del libro mayor", "Contabilidad");
        crearPermisoSiNoExiste("REPORTE_IVA", "Reporte IVA", "/reportes/iva/**", 
                "Consulta del reporte de IVA", "Contabilidad");
        crearPermisoSiNoExiste("AUDITORIA", "Auditoría", "/auditoria/**", 
                "Acceso a los registros de auditoría", "Contabilidad");

        // Categoría: Comercial
        crearPermisoSiNoExiste("PROVEEDORES", "Proveedores", "/proveedores/**", 
                "Gestión de proveedores", "Comercial");
        crearPermisoSiNoExiste("PRODUCTOS", "Productos", "/productos/**", 
                "Gestión de productos y catálogo", "Comercial");
        crearPermisoSiNoExiste("VENTAS", "Ventas", "/ventas/**", 
                "Registro y gestión de ventas", "Comercial");
        crearPermisoSiNoExiste("SOLICITUDES_COMPRA", "Solicitudes de Compra", "/solicitudes-compra/**", 
                "Gestión de solicitudes de compra", "Comercial");
        crearPermisoSiNoExiste("PRESUPUESTOS", "Presupuestos", "/presupuestos/**", 
                "Gestión de presupuestos y cotizaciones", "Comercial");
        crearPermisoSiNoExiste("ORDENES_COMPRA", "Órdenes de Compra", "/ordenes-compra/**", 
                "Gestión de órdenes de compra", "Comercial");
        crearPermisoSiNoExiste("FACTURAS", "Facturas", "/facturas/**", 
                "Gestión de facturas", "Comercial");
        crearPermisoSiNoExiste("PEDIDOS_COTIZACION", "Pedidos de Cotización", "/pedidos-cotizacion/**", 
                "Gestión de pedidos de cotización", "Comercial");

        // Categoría: Depósito
        crearPermisoSiNoExiste("REMITOS", "Remitos", "/remitos/**", 
                "Gestión de remitos de mercadería", "Depósito");
        crearPermisoSiNoExiste("ALMACENES", "Almacenes", "/almacenes/**", 
                "Gestión de almacenes y stock", "Depósito");
        crearPermisoSiNoExiste("CATEGORIAS", "Categorías de Producto", "/categorias/**", 
                "Gestión de categorías de productos", "Depósito");

        // Categoría: Administración
        crearPermisoSiNoExiste("USUARIOS", "Gestión de Usuarios", "/admin/usuarios/**", 
                "Administración de usuarios del sistema", "Administración");
        crearPermisoSiNoExiste("ROLES", "Gestión de Roles", "/admin/roles/**", 
                "Administración de roles y permisos", "Administración");

        logger.info("Permisos del sistema inicializados.");
    }

    private Permiso crearPermisoSiNoExiste(String codigo, String nombre, String urlPattern, 
                                           String descripcion, String categoria) {
        Optional<Permiso> permisoOpt = permisoRepository.findByCodigo(codigo);
        if (permisoOpt.isEmpty()) {
            Permiso permiso = new Permiso(codigo, nombre, urlPattern, descripcion, categoria);
            logger.info("Creando permiso: " + codigo);
            return permisoRepository.save(permiso);
        }
        return permisoOpt.get();
    }

    private void asignarPermisosARoles(Rol contadorRol, Rol depositoRol, Rol comercialRol) {
        logger.info("Asignando permisos por defecto a roles...");

        // Solo asignar si el rol no tiene permisos aún
        if (contadorRol.getPermisos().isEmpty()) {
            asignarPermisosAContador(contadorRol);
        }
        
        if (depositoRol.getPermisos().isEmpty()) {
            asignarPermisosADeposito(depositoRol);
        }
        
        if (comercialRol.getPermisos().isEmpty()) {
            asignarPermisosAComercial(comercialRol);
        }

        logger.info("Permisos por defecto asignados.");
    }

    private void asignarPermisosAContador(Rol rol) {
        List<String> codigosPermisos = Arrays.asList(
            "CUENTAS", "ASIENTOS", "LIBRO_DIARIO", "LIBRO_MAYOR", "REPORTE_IVA", "AUDITORIA"
        );
        asignarPermisosPorCodigo(rol, codigosPermisos);
        logger.info("Permisos de CONTADOR asignados: " + codigosPermisos);
    }

    private void asignarPermisosADeposito(Rol rol) {
        List<String> codigosPermisos = Arrays.asList(
            "REMITOS", "ALMACENES", "PRODUCTOS", "CATEGORIAS", "SOLICITUDES_COMPRA", "ORDENES_COMPRA"
        );
        asignarPermisosPorCodigo(rol, codigosPermisos);
        logger.info("Permisos de DEPOSITO asignados: " + codigosPermisos);
    }

    private void asignarPermisosAComercial(Rol rol) {
        List<String> codigosPermisos = Arrays.asList(
            "PROVEEDORES", "PRODUCTOS", "VENTAS", "SOLICITUDES_COMPRA", 
            "PRESUPUESTOS", "ORDENES_COMPRA", "FACTURAS", "PEDIDOS_COTIZACION", "REMITOS"
        );
        asignarPermisosPorCodigo(rol, codigosPermisos);
        logger.info("Permisos de COMERCIAL asignados: " + codigosPermisos);
    }

    private void asignarPermisosPorCodigo(Rol rol, List<String> codigosPermisos) {
        for (String codigo : codigosPermisos) {
            permisoRepository.findByCodigo(codigo).ifPresent(permiso -> {
                rol.addPermiso(permiso);
            });
        }
        rolRepository.save(rol);
    }
}


