package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.dto.factura.FacturaResponseDTO;
import com.smartbite.operativo.exception.InvalidStateException;
import com.smartbite.operativo.exception.ResourceNotFoundException;
import com.smartbite.operativo.mapper.FacturaMapper;
import com.smartbite.operativo.model.Cliente;
import com.smartbite.operativo.model.Factura;
import com.smartbite.operativo.model.Venta;
import com.smartbite.operativo.repository.ClienteRepository;
import com.smartbite.operativo.repository.FacturaRepository;
import com.smartbite.operativo.repository.VentaRepository;
import com.smartbite.operativo.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final FacturaMapper facturaMapper;

    @Override
    @Transactional
    public FacturaResponseDTO generarFactura(Long ventaId, Long clienteId, BigDecimal impuestos) {

        if (ventaId == null) {
            throw new InvalidStateException("ventaId es obligatorio");
        }

        if (clienteId == null) {
            throw new InvalidStateException("clienteId es obligatorio");
        }

        // 🔹 Idempotencia: si ya existe, retornar la existente
        if (facturaRepository.existsByVentaId(ventaId)) {
            Factura existente = facturaRepository.findByVentaId(ventaId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se pudo recuperar la factura existente para venta id: " + ventaId));

            return facturaMapper.toResponseDTO(existente);
        }

        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta no encontrada con id: " + ventaId));

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + clienteId));

        BigDecimal impuestosCalculados = impuestos == null ? BigDecimal.ZERO : impuestos;

        if (impuestosCalculados.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidStateException("Los impuestos no pueden ser negativos");
        }

        BigDecimal subtotal = venta.getTotal() == null ? BigDecimal.ZERO : venta.getTotal();
        BigDecimal total = subtotal.add(impuestosCalculados);

        Factura factura = Factura.builder()
                .numero(generarNumeroFactura())
                .fechaEmision(LocalDateTime.now())
                .subtotal(subtotal)
                .impuestos(impuestosCalculados)
                .total(total)
                .venta(venta)
                .cliente(cliente)
                .build();

        Factura facturaGuardada;

        try {
            facturaGuardada = facturaRepository.save(factura);
        } catch (DataIntegrityViolationException e) {
            // 🔹 Protección ante concurrencia
            Factura existente = facturaRepository.findByVentaId(ventaId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Error recuperando factura tras conflicto de concurrencia"));

            return facturaMapper.toResponseDTO(existente);
        }

        return facturaMapper.toResponseDTO(facturaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponseDTO obtenerPorId(Long facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Factura no encontrada con id: " + facturaId));
        return facturaMapper.toResponseDTO(factura);
    }

    private String generarNumeroFactura() {
        // TODO: reemplazar por generador de numeración secuencial o UUID controlado en producción
        return "FAC-" + System.currentTimeMillis();
    }
}