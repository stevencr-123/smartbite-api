package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.mesa.ActualizarEstadoMesaRequestDTO;
import com.smartbite.operativo.dto.mesa.MesaResponseDTO;

import java.util.List;

public interface MesaService {

    List<MesaResponseDTO> obtenerTodas();

    List<MesaResponseDTO> obtenerPorSucursal(Long sucursalId);

    MesaResponseDTO obtenerPorId(Long mesaId);

    MesaResponseDTO actualizarEstado(Long mesaId, ActualizarEstadoMesaRequestDTO request);
}

