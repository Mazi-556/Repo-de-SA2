package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.Permiso;
import com.example.SA2Gemini.entity.Rol;
import com.example.SA2Gemini.entity.Usuario;
import com.example.SA2Gemini.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró el usuario: " + username));

        Rol rol = usuario.getRol();
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Agregar el rol como authority (ROLE_ADMIN, ROLE_CONTADOR, etc.)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getName()));
        
        // Agregar todos los permisos del rol como authorities (PERM_CUENTAS, PERM_VENTAS, etc.)
        for (Permiso permiso : rol.getPermisos()) {
            authorities.add(new SimpleGrantedAuthority("PERM_" + permiso.getCodigo()));
        }
        
        return new User(usuario.getUsername(), usuario.getPassword(), authorities);
    }
}
