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

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Add this import

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enable method-level security
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                // CONTADOR: Todo lo contable (plan de cuentas, asientos, libro diario y mayor)
                .requestMatchers("/cuentas/**", "/asientos/**", "/libro-diario/**", "/libro-mayor/**", "/auditoria/**").hasAnyRole("CONTADOR", "ADMIN")
                
                // COMERCIAL: Ventas, pedidos, presupuestos, órdenes de compra, facturas, productos y precios
                .requestMatchers("/ventas/**", "/pedidos-cotizacion/**", "/presupuestos/**", "/ordenes-compra/**", "/facturas/**", "/productos/**", "/proveedores/**").hasAnyRole("COMERCIAL", "ADMIN")
                
                // DEPOSITO: Remitos y solicitudes de compra
                .requestMatchers("/remitos/**", "/solicitudes-compra/**", "/almacenes/**").hasAnyRole("DEPOSITO", "ADMIN")
                
                // ADMIN: Acceso a todo
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                
                .requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/favicon.ico").permitAll()
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
                );
        return http.build();
    }
}
