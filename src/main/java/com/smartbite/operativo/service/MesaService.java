package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.mesa.ActualizarEstadoMesaRequestDTO;
import com.smartbite.operativo.dto.mesa.CrearMesaRequestDTO;
import com.smartbite.operativo.dto.mesa.MesaResponseDTO;
import com.smartbite.operativo.model.enums.EstadoMesa;

import java.util.List;

public interface MesaService {

    List<MesaResponseDTO> obtenerTodas();

    List<MesaResponseDTO> obtenerPorSucursal(Long sucursalId);

    List<MesaResponseDTO> obtenerPorSucursalYEstado(
            Long sucursalId,
            EstadoMesa estado
    );

    List<MesaResponseDTO> obtenerDisponibles(Long sucursalId);

    MesaResponseDTO obtenerPorId(Long mesaId);

    MesaResponseDTO crearMesa(CrearMesaRequestDTO request);

    MesaResponseDTO actualizarMesa(
            Long id,
            CrearMesaRequestDTO request
    );

    MesaResponseDTO actualizarEstado(
            Long mesaId,
            ActualizarEstadoMesaRequestDTO request
    );

    MesaResponseDTO cambiarEstadoActivo(
            Long mesaId,
            Boolean activa
    );

    void eliminarMesa(Long id);
}