package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.pago.CrearPagoRequestDTO;
import com.smartbite.operativo.dto.pago.PagoResponseDTO;
import com.smartbite.operativo.exception.BusinessException;
import com.smartbite.operativo.exception.ResourceNotFoundException;
import com.smartbite.operativo.mapper.PagoMapper;
import com.smartbite.operativo.model.MetodoPago;
import com.smartbite.operativo.model.Orden;
import com.smartbite.operativo.model.Pago;
import com.smartbite.operativo.model.enums.EstadoOrden;
import com.smartbite.operativo.model.enums.EstadoPago;
import com.smartbite.operativo.repository.MetodoPagoRepository;
import com.smartbite.operativo.repository.OrdenRepository;
import com.smartbite.operativo.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final OrdenRepository ordenRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PagoMapper pagoMapper;

    @Override
    @Transactional
    public PagoResponseDTO registrarPago(CrearPagoRequestDTO request) {
        Orden orden = ordenRepository.findById(request.getOrdenId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orden no encontrada con id: " + request.getOrdenId()));

        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new BusinessException(
                    "No se puede registrar un pago para una orden cancelada");
        }

        MetodoPago metodoPago = metodoPagoRepository.findById(request.getMetodoPagoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Método de pago no encontrado con id: " + request.getMetodoPagoId()));

        Pago pago = Pago.builder()
                .monto(request.getMonto())
                .fechaPago(LocalDateTime.now())
                .estado(EstadoPago.PENDIENTE)
                .referenciaTransaccion(request.getReferenciaTransaccion())
                .orden(orden)
                .metodoPago(metodoPago)
                .build();

        Pago pagoGuardado = pagoRepository.save(pago);
        return pagoMapper.toResponseDTO(pagoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> obtenerPagosPorOrden(Long ordenId) {
        if (!ordenRepository.existsById(ordenId)) {
            throw new ResourceNotFoundException("Orden no encontrada con id: " + ordenId);
        }

        return pagoRepository.findByOrdenId(ordenId)
                .stream()
                .map(pagoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPagado(Long ordenId) {
        if (!ordenRepository.existsById(ordenId)) {
            throw new ResourceNotFoundException("Orden no encontrada con id: " + ordenId);
        }

        return pagoRepository.findByOrdenIdAndEstado(ordenId, EstadoPago.APROBADO)
                .stream()
                .map(pago -> Objects.requireNonNullElse(pago.getMonto(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaOrdenTotalmentePagada(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orden no encontrada con id: " + ordenId));

        BigDecimal totalPagado = calcularTotalPagado(ordenId);
        return totalPagado.compareTo(orden.getTotal()) >= 0;
    }
}
