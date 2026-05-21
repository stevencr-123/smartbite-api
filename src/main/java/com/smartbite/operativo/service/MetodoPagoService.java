package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.metodopago.MetodoPagoResponseDTO;

import java.util.List;

public interface MetodoPagoService {

    List<MetodoPagoResponseDTO> obtenerTodos();

    List<MetodoPagoResponseDTO> obtenerActivos();

    MetodoPagoResponseDTO obtenerPorId(Long id);
}