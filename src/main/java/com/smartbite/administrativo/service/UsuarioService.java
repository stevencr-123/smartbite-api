package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.UsuarioRequestDTO;
import com.smartbite.administrativo.dto.UsuarioResponseDTO;
import java.util.List;

public interface UsuarioService {

    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO requestDTO);

    UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO requestDTO);

    UsuarioResponseDTO obtenerUsuarioPorId(Long id);

    List<UsuarioResponseDTO> obtenerTodosLosUsuarios();

    List<UsuarioResponseDTO> obtenerUsuariosPorSucursal(Long sucursalId);

    List<UsuarioResponseDTO> obtenerUsuariosPorRol(Long rolId);

    void eliminarUsuario(Long id);

    UsuarioResponseDTO activarDesactivarUsuario(Long id, Boolean activo);


}
