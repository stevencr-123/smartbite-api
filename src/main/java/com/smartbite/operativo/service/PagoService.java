package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.pago.CrearPagoRequestDTO;
import com.smartbite.operativo.dto.pago.PagoResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PagoService {

    PagoResponseDTO registrarPago(CrearPagoRequestDTO request);

    List<PagoResponseDTO> obtenerPagosPorOrden(Long ordenId);

    BigDecimal calcularTotalPagado(Long ordenId);

    boolean estaOrdenTotalmentePagada(Long ordenId);
}

