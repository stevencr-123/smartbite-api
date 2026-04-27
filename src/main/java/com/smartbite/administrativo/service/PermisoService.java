package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.PermisoRequestDTO;
import com.smartbite.administrativo.dto.PermisoResponseDTO;
import java.util.List;

public interface PermisoService {

    PermisoResponseDTO crearPermiso(PermisoRequestDTO requestDTO);

    PermisoResponseDTO actualizarPermiso(Long id, PermisoRequestDTO requestDTO);

    PermisoResponseDTO obtenerPermisoPorId(Long id);

    List<PermisoResponseDTO> obtenerTodosLosPermisos();

    void eliminarPermiso(Long id);

    PermisoResponseDTO activarDesactivarPermiso(Long id, Boolean activo);

    List<PermisoResponseDTO> obtenerPermisosPorRecurso(String recurso);

    List<PermisoResponseDTO> obtenerPermisosPorAccion(String accion);
}