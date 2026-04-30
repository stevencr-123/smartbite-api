package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.RolRequestDTO;
import com.smartbite.administrativo.dto.RolResponseDTO;
import java.util.List;

public interface RolService {

    RolResponseDTO crearRol(RolRequestDTO requestDTO);

    RolResponseDTO actualizarRol(Long id, RolRequestDTO requestDTO);

    RolResponseDTO obtenerRolPorId(Long id);

    List<RolResponseDTO> obtenerTodosLosRoles();

    void eliminarRol(Long id);

    RolResponseDTO activarDesactivarRol(Long id, Boolean activo);

    RolResponseDTO asignarPermisos(Long rolId, List<Long> permisosIds);

    RolResponseDTO quitarPermiso(Long rolId, Long permisoId);
}