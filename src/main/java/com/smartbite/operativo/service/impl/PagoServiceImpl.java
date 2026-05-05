package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.dto.pago.CrearPagoRequestDTO;
import com.smartbite.operativo.dto.pago.PagoResponseDTO;
import com.smartbite.operativo.exception.*;
import com.smartbite.operativo.mapper.PagoMapper;
import com.smartbite.operativo.model.*;
import com.smartbite.operativo.model.enums.EstadoOrden;
import com.smartbite.operativo.model.enums.EstadoPago;
import com.smartbite.operativo.repository.*;
import com.smartbite.operativo.service.PagoService;
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

        if (request.getOrdenId() == null) {
            throw new BusinessException("ordenId obligatorio");
        }

        Orden orden = ordenRepository.findById(request.getOrdenId())
                .orElseThrow(() -> new OrdenNotFoundException("Orden no encontrada"));

        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new BusinessException("Orden cancelada");
        }

        if (estaOrdenTotalmentePagada(request.getOrdenId())) {
            throw new BusinessException("Orden ya pagada");
        }

        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Monto inválido");
        }

        BigDecimal totalPagado = calcularTotalPagado(request.getOrdenId());
        BigDecimal saldo = orden.getTotal().subtract(totalPagado);

        if (request.getMonto().compareTo(saldo) > 0) {
            throw new BusinessException("Monto excede saldo");
        }

        MetodoPago metodo = metodoPagoRepository.findById(request.getMetodoPagoId())
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado"));

        Pago pago = Pago.builder()
                .monto(request.getMonto())
                .fechaPago(LocalDateTime.now())
                .estado(EstadoPago.APROBADO)
                .orden(orden)
                .metodoPago(metodo)
                .referenciaTransaccion(request.getReferenciaTransaccion())
                .build();

        Pago guardado = pagoRepository.save(pago);

        if (totalPagado.add(request.getMonto()).compareTo(orden.getTotal()) == 0) {
            orden.setEstado(EstadoOrden.PAGADA);
            ordenRepository.save(orden);
        }

        return pagoMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> obtenerPagosPorOrden(Long ordenId) {
        if (!ordenRepository.existsById(ordenId)) {
            throw new OrdenNotFoundException("Orden no encontrada");
        }

        return pagoRepository.findByOrdenId(ordenId)
                .stream()
                .map(pagoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPagado(Long ordenId) {
        return pagoRepository.findByOrdenIdAndEstado(ordenId, EstadoPago.APROBADO)
                .stream()
                .map(p -> Objects.requireNonNullElse(p.getMonto(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaOrdenTotalmentePagada(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new OrdenNotFoundException("Orden no encontrada"));

        return calcularTotalPagado(ordenId).compareTo(orden.getTotal()) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO obtenerPagoPorId(Long pagoId) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new PagoNotFoundException("Pago no encontrado"));

        return pagoMapper.toResponseDTO(pago);
    }
}