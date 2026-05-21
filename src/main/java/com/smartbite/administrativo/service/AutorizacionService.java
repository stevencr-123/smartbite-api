package com.smartbite.administrativo.service;

import com.smartbite.administrativo.enums.RolNombre;
import com.smartbite.administrativo.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class AutorizacionService {

    /**
     * Verifica si un usuario tiene un permiso específico
     */
    public boolean tienePermiso(Usuario usuario, String permiso) {
        if (usuario == null || usuario.getRol() == null || usuario.getRol().getPermisos() == null) {
            return false;
        }
        return usuario.getRol().getPermisos().stream()
                .anyMatch(p -> p.getNombre().equals(permiso));
    }

    /**
     * Verifica si el usuario es ADMINISTRADOR
     */
    public boolean esAdministrador(Usuario usuario) {
        return usuario != null && usuario.getRol() != null
                && usuario.getRol().getNombre() == RolNombre.ADMINISTRADOR;
    }
}