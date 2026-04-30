package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.SucursalRequestDTO;
import com.smartbite.administrativo.dto.SucursalResponseDTO;
import java.util.List;

public interface SucursalService {

    SucursalResponseDTO crearSucursal(SucursalRequestDTO requestDTO);

    SucursalResponseDTO actualizarSucursal(Long id, SucursalRequestDTO requestDTO);

    SucursalResponseDTO obtenerSucursalPorId(Long id);

    List<SucursalResponseDTO> obtenerSucursalesPorRestaurante(Long restauranteId);

    List<SucursalResponseDTO> obtenerTodasLasSucursales();

    void eliminarSucursal(Long id);

    SucursalResponseDTO activarDesactivarSucursal(Long id, Boolean activo);
}