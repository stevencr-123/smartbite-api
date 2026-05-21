package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.MovimientoInventarioRequestDTO;
import com.smartbite.administrativo.dto.MovimientoInventarioResponseDTO;
import com.smartbite.administrativo.enums.TipoMovimientoInventario;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.model.ItemInventario;
import com.smartbite.administrativo.model.MovimientoInventario;
import com.smartbite.administrativo.repository.ItemInventarioRepository;
import com.smartbite.administrativo.repository.MovimientoInventarioRepository;
import com.smartbite.administrativo.service.MovimientoInventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final ItemInventarioRepository itemInventarioRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Override
    public MovimientoInventarioResponseDTO registrarMovimiento(MovimientoInventarioRequestDTO requestDTO) {
        log.info("Registrando movimiento de inventario: tipo={}, cantidad={}",
                requestDTO.getTipo(), requestDTO.getCantidad());

        ItemInventario item = itemInventarioRepository.findById(requestDTO.getItemInventarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Ítem de inventario no encontrado con ID: " + requestDTO.getItemInventarioId()));

        if (!item.getActivo()) {
            throw new BusinessException("No se puede registrar movimiento para un ítem inactivo");
        }

        int stockAntes = item.getStockActual();
        int stockDespues = stockAntes;

        switch (requestDTO.getTipo()) {
            case ENTRADA:
                stockDespues = stockAntes + requestDTO.getCantidad();
                break;
            case SALIDA:
                if (stockAntes < requestDTO.getCantidad()) {
                    throw new BusinessException("Stock insuficiente. Stock actual: " + stockAntes);
                }
                stockDespues = stockAntes - requestDTO.getCantidad();
                break;
            case AJUSTE:
            case MERMA:
                if (stockAntes < requestDTO.getCantidad()) {
                    throw new BusinessException("Stock insuficiente para ajuste/merma");
                }
                stockDespues = stockAntes - requestDTO.getCantidad();
                break;
        }

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipo(requestDTO.getTipo());
        movimiento.setCantidad(requestDTO.getCantidad());
        movimiento.setMotivo(requestDTO.getMotivo());
        movimiento.setReferencia(requestDTO.getReferencia());
        movimiento.setReferenciaId(requestDTO.getReferenciaId());
        movimiento.setStockAntes(stockAntes);
        movimiento.setStockDespues(stockDespues);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setItemInventario(item);

        movimientoInventarioRepository.save(movimiento);

        item.setStockActual(stockDespues);
        itemInventarioRepository.save(item);

        log.info("Movimiento registrado. Stock: {} → {}", stockAntes, stockDespues);

        return convertToResponseDTO(movimiento, item);
    }

    @Override
    public MovimientoInventarioResponseDTO registrarEntrada(Long itemInventarioId, Integer cantidad, String motivo, String referencia, Long referenciaId) {
        MovimientoInventarioRequestDTO request = new MovimientoInventarioRequestDTO();
        request.setItemInventarioId(itemInventarioId);
        request.setTipo(TipoMovimientoInventario.ENTRADA);
        request.setCantidad(cantidad);
        request.setMotivo(motivo);
        request.setReferencia(referencia);
        request.setReferenciaId(referenciaId);
        return registrarMovimiento(request);
    }

    @Override
    public MovimientoInventarioResponseDTO registrarSalida(Long itemInventarioId, Integer cantidad, String motivo, String referencia, Long referenciaId) {
        MovimientoInventarioRequestDTO request = new MovimientoInventarioRequestDTO();
        request.setItemInventarioId(itemInventarioId);
        request.setTipo(TipoMovimientoInventario.SALIDA);
        request.setCantidad(cantidad);
        request.setMotivo(motivo);
        request.setReferencia(referencia);
        request.setReferenciaId(referenciaId);
        return registrarMovimiento(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponseDTO> obtenerMovimientosPorItem(Long itemInventarioId) {
        log.debug("Obteniendo movimientos del ítem ID: {}", itemInventarioId);

        return movimientoInventarioRepository.findByItemInventarioId(itemInventarioId).stream()
                .map(m -> convertToResponseDTO(m, m.getItemInventario()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponseDTO> obtenerMovimientosPorTipo(TipoMovimientoInventario tipo) {
        log.debug("Obteniendo movimientos por tipo: {}", tipo);

        return movimientoInventarioRepository.findByTipo(tipo).stream()
                .map(m -> convertToResponseDTO(m, m.getItemInventario()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponseDTO> obtenerMovimientosPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Obteniendo movimientos entre {} y {}", inicio, fin);

        return movimientoInventarioRepository.findByFechaMovimientoBetween(inicio, fin).stream()
                .map(m -> convertToResponseDTO(m, m.getItemInventario()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoInventarioResponseDTO obtenerMovimientoPorId(Long id) {
        log.debug("Obteniendo movimiento por ID: {}", id);

        MovimientoInventario movimiento = movimientoInventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con ID: " + id));

        return convertToResponseDTO(movimiento, movimiento.getItemInventario());
    }


    private MovimientoInventarioResponseDTO convertToResponseDTO(MovimientoInventario movimiento, ItemInventario item) {
        MovimientoInventarioResponseDTO response = new MovimientoInventarioResponseDTO();
        response.setId(movimiento.getId());
        response.setTipo(movimiento.getTipo());
        response.setCantidad(movimiento.getCantidad());
        response.setMotivo(movimiento.getMotivo());
        response.setReferencia(movimiento.getReferencia());
        response.setReferenciaId(movimiento.getReferenciaId());
        response.setStockAntes(movimiento.getStockAntes());
        response.setStockDespues(movimiento.getStockDespues());
        response.setFechaMovimiento(movimiento.getFechaMovimiento());
        response.setFechaCreacion(movimiento.getFechaCreacion());

        if (item != null) {
            response.setItemInventarioId(item.getId());
            response.setItemInventarioNombre(item.getNombre());
            response.setItemInventarioUnidadMedida(item.getUnidadMedida());
        }

        return response;
    }
}