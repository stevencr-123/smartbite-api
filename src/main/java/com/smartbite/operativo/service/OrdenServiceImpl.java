package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.detalle.AgregarProductoRequestDTO;
import com.smartbite.operativo.dto.detalle.DetalleOrdenResponseDTO;
import com.smartbite.operativo.dto.orden.CrearOrdenRequestDTO;
import com.smartbite.operativo.dto.orden.OrdenResumenDTO;
import com.smartbite.operativo.dto.orden.OrdenResponseDTO;
import com.smartbite.operativo.exception.InvalidStateException;
import com.smartbite.operativo.exception.ResourceNotFoundException;
import com.smartbite.operativo.mapper.DetalleOrdenMapper;
import com.smartbite.operativo.mapper.OrdenMapper;
import com.smartbite.operativo.model.DetalleOrden;
import com.smartbite.operativo.model.Mesa;
import com.smartbite.operativo.model.Orden;
import com.smartbite.operativo.model.enums.EstadoMesa;
import com.smartbite.operativo.model.enums.EstadoOrden;
import com.smartbite.operativo.repository.DetalleOrdenRepository;
import com.smartbite.operativo.repository.MesaRepository;
import com.smartbite.operativo.repository.OrdenRepository;
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
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final MesaRepository mesaRepository;
    private final OrdenMapper ordenMapper;
    private final DetalleOrdenMapper detalleOrdenMapper;

    @Override
    @Transactional
    public OrdenResponseDTO crearOrden(CrearOrdenRequestDTO request) {
        Mesa mesa = mesaRepository.findById(request.getMesaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mesa no encontrada con id: " + request.getMesaId()));

        Orden orden = Orden.builder()
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoOrden.PENDIENTE)
                .total(BigDecimal.ZERO)
                .mesa(mesa)
                .sucursalId(request.getSucursalId())
                .usuarioId(request.getUsuarioId())
                .build();

        // Add initial products if provided
        if (request.getProductos() != null && !request.getProductos().isEmpty()) {
            for (AgregarProductoRequestDTO producto : request.getProductos()) {
                DetalleOrden detalle = DetalleOrden.builder()
                        .productoId(producto.getProductoId())
                        .cantidad(producto.getCantidad())
                        .precioUnitario(BigDecimal.ZERO) // Price will be set by administrativo module
                        .subtotal(BigDecimal.ZERO)
                        .orden(orden)
                        .build();
                orden.getDetalles().add(detalle);
            }
        }

        // Mark table as occupied
        mesa.setEstado(EstadoMesa.OCUPADA);
        mesaRepository.save(mesa);

        Orden ordenGuardada = ordenRepository.save(orden);
        return ordenMapper.toResponseDTO(ordenGuardada);
    }

    @Override
    @Transactional
    public DetalleOrdenResponseDTO agregarProducto(Long ordenId, AgregarProductoRequestDTO request) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orden no encontrada con id: " + ordenId));

        if (orden.getEstado() == EstadoOrden.CANCELADA || orden.getEstado() == EstadoOrden.PAGADA) {
            throw new InvalidStateException(
                    "No se pueden agregar productos a una orden en estado: " + orden.getEstado());
        }

        DetalleOrden detalle = DetalleOrden.builder()
                .productoId(request.getProductoId())
                .cantidad(request.getCantidad())
                .precioUnitario(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .orden(orden)
                .build();

        DetalleOrden detalleGuardado = detalleOrdenRepository.save(detalle);
        return detalleOrdenMapper.toResponseDTO(detalleGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenResponseDTO obtenerOrdenPorId(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orden no encontrada con id: " + ordenId));
        return ordenMapper.toResponseDTO(orden);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenResumenDTO> obtenerOrdenesActivas() {
        List<EstadoOrden> estadosActivos = List.of(
                EstadoOrden.PENDIENTE,
                EstadoOrden.EN_PREPARACION,
                EstadoOrden.LISTA,
                EstadoOrden.ENTREGADA
        );

        return ordenRepository.findByEstadoIn(estadosActivos)
                .stream()
                .map(ordenMapper::toResumenDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrdenResponseDTO recalcularTotal(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orden no encontrada con id: " + ordenId));

        BigDecimal nuevoTotal = orden.getDetalles()
                .stream()
                .map(detalle -> Objects.requireNonNullElse(detalle.getSubtotal(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        orden.setTotal(nuevoTotal);
        Orden ordenActualizada = ordenRepository.save(orden);
        return ordenMapper.toResponseDTO(ordenActualizada);
    }

    @Override
    @Transactional
    public OrdenResponseDTO cambiarEstado(Long ordenId, String nuevoEstado) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orden no encontrada con id: " + ordenId));

        EstadoOrden estado;
        try {
            estado = EstadoOrden.valueOf(nuevoEstado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStateException("Estado de orden inválido: " + nuevoEstado);
        }

        orden.setEstado(estado);
        Orden ordenActualizada = ordenRepository.save(orden);
        return ordenMapper.toResponseDTO(ordenActualizada);
    }

    @Override
    @Transactional
    public OrdenResponseDTO cerrarOrden(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orden no encontrada con id: " + ordenId));

        if (orden.getEstado() == EstadoOrden.CANCELADA || orden.getEstado() == EstadoOrden.PAGADA) {
            throw new InvalidStateException(
                    "La orden ya se encuentra en estado: " + orden.getEstado());
        }

        orden.setEstado(EstadoOrden.PAGADA);
        Orden ordenCerrada = ordenRepository.save(orden);

        // Release the table
        Mesa mesa = orden.getMesa();
        mesa.setEstado(EstadoMesa.DISPONIBLE);
        mesaRepository.save(mesa);

        return ordenMapper.toResponseDTO(ordenCerrada);
    }
}
