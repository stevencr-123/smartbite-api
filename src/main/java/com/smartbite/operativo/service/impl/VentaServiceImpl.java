package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.dto.venta.VentaResponseDTO;
import com.smartbite.operativo.exception.InvalidStateException;
import com.smartbite.operativo.exception.OrdenNotFoundException;
import com.smartbite.operativo.exception.ResourceNotFoundException;
import com.smartbite.operativo.mapper.VentaMapper;
import com.smartbite.operativo.model.Orden;
import com.smartbite.operativo.model.Venta;
import com.smartbite.operativo.repository.OrdenRepository;
import com.smartbite.operativo.repository.VentaRepository;
import com.smartbite.operativo.service.FacturaService;
import com.smartbite.operativo.service.PagoService;
import com.smartbite.operativo.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final OrdenRepository ordenRepository;
    private final PagoService pagoService;
    private final FacturaService facturaService;
    private final VentaMapper ventaMapper;

    @Override
    @Transactional
    public VentaResponseDTO crearVentaDesdeOrden(Long ordenId, Long clienteId) {

        if (ordenId == null) {
            throw new InvalidStateException("ordenId es obligatorio");
        }

        // Punto único de cierre de flujo comercial:
        // 1. Validar pago completo vía PagoService
        // 2. Crear venta idempotente
        // 3. Generar factura si aplica

        // 🔹 Idempotencia: si ya existe venta, reutilizar
        if (ventaRepository.existsByOrdenId(ordenId)) {
            Venta ventaExistente = ventaRepository.findByOrdenId(ordenId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se pudo recuperar la venta existente para orden id: " + ordenId));

            generarFacturaSiAplica(ventaExistente.getId(), clienteId);
            return ventaMapper.toResponseDTO(ventaExistente);
        }

        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new OrdenNotFoundException(
                        "Orden no encontrada con id: " + ordenId));

        // 🔹 Fuente única de verdad: pagos
        if (!pagoService.estaOrdenTotalmentePagada(ordenId)) {
            throw new InvalidStateException("Solo se puede generar venta de una orden totalmente pagada");
        }

        Venta venta = Venta.builder()
                .fechaVenta(LocalDateTime.now())
                .total(orden.getTotal())
                .orden(orden)
                .build();

        Venta ventaGuardada;

        try {
            ventaGuardada = ventaRepository.save(venta);
        } catch (DataIntegrityViolationException e) {
            // 🔹 Protección ante concurrencia
            Venta existente = ventaRepository.findByOrdenId(ordenId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Error recuperando venta tras conflicto de concurrencia"));

            return ventaMapper.toResponseDTO(existente);
        }

        generarFacturaSiAplica(ventaGuardada.getId(), clienteId);

        return ventaMapper.toResponseDTO(ventaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerPorId(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta no encontrada con id: " + ventaId));
        return ventaMapper.toResponseDTO(venta);
    }

    /**
     * Genera factura solo si:
     * - Hay clienteId
     * - No existe ya una factura para la venta
     */
    private void generarFacturaSiAplica(Long ventaId, Long clienteId) {
        if (clienteId != null) {
            try {
                facturaService.generarFactura(ventaId, clienteId, BigDecimal.ZERO);
            } catch (InvalidStateException ignored) {
                // Ya existe factura → comportamiento idempotente
            }
        }
    }
}