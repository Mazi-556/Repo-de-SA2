package com.example.SA2Gemini.service;

import com.example.SA2Gemini.entity.Usuario;
import com.example.SA2Gemini.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró el usuario: " + username));

        return new User(usuario.getUsername(), usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getName())));
    }
}
