package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.venta.VentaResponseDTO;

public interface VentaService {

    VentaResponseDTO crearVentaDesdeOrden(Long ordenId, Long clienteId);

    VentaResponseDTO obtenerPorId(Long ventaId);
}

