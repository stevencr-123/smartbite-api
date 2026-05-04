package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.UsuarioRequestDTO;
import com.smartbite.administrativo.dto.UsuarioResponseDTO;
import com.smartbite.administrativo.enums.RolNombre;
import java.util.List;

public interface UsuarioService {

    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO requestDTO);

    UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO requestDTO);

    UsuarioResponseDTO obtenerUsuarioPorId(Long id);

    List<UsuarioResponseDTO> obtenerTodosLosUsuarios();

    List<UsuarioResponseDTO> obtenerUsuariosPorSucursal(Long sucursalId);

    List<UsuarioResponseDTO> obtenerUsuariosPorRol(RolNombre rolNombre);  // ← CORREGIDO

    List<UsuarioResponseDTO> obtenerUsuariosActivos();

    void eliminarUsuario(Long id);

    UsuarioResponseDTO activarDesactivarUsuario(Long id, Boolean activo);

    boolean existsById(Long id);

    UsuarioResponseDTO asignarRol(Long usuarioId, Long rolId);

}