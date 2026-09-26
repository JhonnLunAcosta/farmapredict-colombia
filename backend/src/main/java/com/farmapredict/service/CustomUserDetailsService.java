package com.farmapredict.service;

import com.farmapredict.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;

    public CustomUserDetailsService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        if (!u.isActivo()) {
            throw new org.springframework.security.authentication.DisabledException("Cuenta desactivada");
        }
        return new User(u.getUsername(), u.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().toUpperCase())));
    }
}
