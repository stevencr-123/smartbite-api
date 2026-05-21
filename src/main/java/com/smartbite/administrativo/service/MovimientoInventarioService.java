package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.MovimientoInventarioRequestDTO;
import com.smartbite.administrativo.dto.MovimientoInventarioResponseDTO;
import com.smartbite.administrativo.enums.TipoMovimientoInventario;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioService {

    // Método principal para registrar movimiento
    MovimientoInventarioResponseDTO registrarMovimiento(MovimientoInventarioRequestDTO requestDTO);

    // Métodos específicos
    MovimientoInventarioResponseDTO registrarEntrada(Long itemInventarioId, Integer cantidad, String motivo, String referencia, Long referenciaId);

    MovimientoInventarioResponseDTO registrarSalida(Long itemInventarioId, Integer cantidad, String motivo, String referencia, Long referenciaId);

    // Métodos de consulta
    List<MovimientoInventarioResponseDTO> obtenerMovimientosPorItem(Long itemInventarioId);

    List<MovimientoInventarioResponseDTO> obtenerMovimientosPorTipo(TipoMovimientoInventario tipo);

    List<MovimientoInventarioResponseDTO> obtenerMovimientosPorRangoFechas(LocalDateTime inicio, LocalDateTime fin);

    MovimientoInventarioResponseDTO obtenerMovimientoPorId(Long id);
}