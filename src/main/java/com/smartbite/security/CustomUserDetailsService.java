package com.smartbite.security;

import com.smartbite.administrativo.model.Usuario;
import com.smartbite.administrativo.repository.UsuarioRepository;
import com.smartbite.administrativo.model.UsuarioSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 👈 importar esto

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        // 👇 Fuerza la carga del rol y sus permisos antes de cerrar la sesión
        if (usuario.getRol() != null) {
            usuario.getRol().getNombre(); // carga el rol
            usuario.getRol().getPermisos().size(); // carga los permisos
        }

        return new UsuarioSecurity(usuario);
    }
}