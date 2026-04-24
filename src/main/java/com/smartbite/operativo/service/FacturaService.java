package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.factura.FacturaResponseDTO;

import java.math.BigDecimal;

public interface FacturaService {

    FacturaResponseDTO generarFactura(Long ventaId, Long clienteId, BigDecimal impuestos);

    FacturaResponseDTO obtenerPorId(Long facturaId);
}

