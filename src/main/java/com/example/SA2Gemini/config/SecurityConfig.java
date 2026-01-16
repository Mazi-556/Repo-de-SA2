package com.example.SA2Gemini.config;

import com.example.SA2Gemini.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                // Recursos estáticos y login - acceso público
                .requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/favicon.ico").permitAll()
                
                // Administración - solo ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Para el resto de las URLs, verificamos permisos dinámicamente
                // Los permisos se verifican por código de permiso (authority)
                
                // Contabilidad
                .requestMatchers("/cuentas/**").hasAnyAuthority("PERM_CUENTAS", "ROLE_ADMIN")
                .requestMatchers("/asientos/**").hasAnyAuthority("PERM_ASIENTOS", "ROLE_ADMIN")
                .requestMatchers("/reportes/libro-diario/**").hasAnyAuthority("PERM_LIBRO_DIARIO", "ROLE_ADMIN")
                .requestMatchers("/reportes/libro-mayor/**").hasAnyAuthority("PERM_LIBRO_MAYOR", "ROLE_ADMIN")
                .requestMatchers("/reportes/iva/**").hasAnyAuthority("PERM_REPORTE_IVA", "ROLE_ADMIN")
                .requestMatchers("/auditoria/**").hasAnyAuthority("PERM_AUDITORIA", "ROLE_ADMIN")
                
                // Comercial
                .requestMatchers("/proveedores/**").hasAnyAuthority("PERM_PROVEEDORES", "ROLE_ADMIN")
                .requestMatchers("/productos/**").hasAnyAuthority("PERM_PRODUCTOS", "ROLE_ADMIN")
                .requestMatchers("/ventas/**").hasAnyAuthority("PERM_VENTAS", "ROLE_ADMIN")
                .requestMatchers("/solicitudes-compra/**").hasAnyAuthority("PERM_SOLICITUDES_COMPRA", "ROLE_ADMIN")
                .requestMatchers("/presupuestos/**").hasAnyAuthority("PERM_PRESUPUESTOS", "ROLE_ADMIN")
                .requestMatchers("/ordenes-compra/**").hasAnyAuthority("PERM_ORDENES_COMPRA", "ROLE_ADMIN")
                .requestMatchers("/facturas/**").hasAnyAuthority("PERM_FACTURAS", "ROLE_ADMIN")
                .requestMatchers("/pedidos-cotizacion/**").hasAnyAuthority("PERM_PEDIDOS_COTIZACION", "ROLE_ADMIN")
                
                // Depósito
                .requestMatchers("/remitos/**").hasAnyAuthority("PERM_REMITOS", "ROLE_ADMIN")
                .requestMatchers("/almacenes/**").hasAnyAuthority("PERM_ALMACENES", "ROLE_ADMIN")
                .requestMatchers("/categorias/**").hasAnyAuthority("PERM_CATEGORIAS", "ROLE_ADMIN")
                
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler)
                );
        return http.build();
    }
}
