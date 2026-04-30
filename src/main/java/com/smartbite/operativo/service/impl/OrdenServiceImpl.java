package com.smartbite.operativo.service.impl;
import com.smartbite.operativo.client.ProductoClient;
import com.smartbite.operativo.client.dto.ProductoDTO;
import com.smartbite.operativo.dto.detalle.AgregarProductoRequestDTO;
import com.smartbite.operativo.dto.detalle.DetalleOrdenResponseDTO;
import com.smartbite.operativo.dto.orden.CrearOrdenRequestDTO;
import com.smartbite.operativo.dto.orden.OrdenResumenDTO;
import com.smartbite.operativo.dto.orden.OrdenResponseDTO;
import com.smartbite.operativo.exception.BusinessException;
import com.smartbite.operativo.exception.EstadoOrdenInvalidoException;
import com.smartbite.operativo.exception.InvalidStateException;
import com.smartbite.operativo.exception.MesaNotFoundException;
import com.smartbite.operativo.exception.OrdenNoPagadaException;
import com.smartbite.operativo.exception.OrdenNotFoundException;
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
import com.smartbite.operativo.service.OrdenService;
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
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final MesaRepository mesaRepository;
    private final ProductoClient productoClient;
    private final PagoService pagoService;
    private final OrdenMapper ordenMapper;
    private final DetalleOrdenMapper detalleOrdenMapper;

    @Override
    @Transactional
    public OrdenResponseDTO crearOrden(CrearOrdenRequestDTO request) {
        Mesa mesa = mesaRepository.findById(request.getMesaId())
                .orElseThrow(() -> new MesaNotFoundException(
                        "Mesa no encontrada con id: " + request.getMesaId()));

        Orden orden = Orden.builder()
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoOrden.PENDIENTE)
                .total(BigDecimal.ZERO)
                .mesa(mesa)
                .sucursalId(request.getSucursalId())
                .usuarioId(request.getUsuarioId())
                .build();

        // Add initial products with snapshot pricing.
        if (request.getProductos() != null && !request.getProductos().isEmpty()) {
            for (AgregarProductoRequestDTO producto : request.getProductos()) {
                DetalleOrden detalle = crearDetalleConPrecioSnapshot(orden, producto);
                orden.getDetalles().add(detalle);
            }
        }

        BigDecimal total = orden.getDetalles()
                .stream()
                .map(detalle -> Objects.requireNonNullElse(detalle.getSubtotal(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orden.setTotal(total);

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
                .orElseThrow(() -> new OrdenNotFoundException(
                        "Orden no encontrada con id: " + ordenId));

        if (orden.getEstado() == EstadoOrden.CANCELADA || orden.getEstado() == EstadoOrden.PAGADA) {
            throw new InvalidStateException(
                    "No se pueden agregar productos a una orden en estado: " + orden.getEstado());
        }

        DetalleOrden detalle = crearDetalleConPrecioSnapshot(orden, request);

        // Keep entity graph consistent
        orden.getDetalles().add(detalle);

        DetalleOrden detalleGuardado = detalleOrdenRepository.save(detalle);
        BigDecimal nuevoTotal = orden.getDetalles().stream()
                .map(item -> Objects.requireNonNullElse(item.getSubtotal(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orden.setTotal(nuevoTotal);
        ordenRepository.save(orden);
        return detalleOrdenMapper.toResponseDTO(detalleGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenResponseDTO obtenerOrdenPorId(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new OrdenNotFoundException(
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
                .orElseThrow(() -> new OrdenNotFoundException(
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
    public OrdenResponseDTO cambiarEstado(Long ordenId, EstadoOrden nuevoEstado) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new OrdenNotFoundException(
                        "Orden no encontrada con id: " + ordenId));

        EstadoOrden estadoActual = orden.getEstado();
        if (!esTransicionValida(estadoActual, nuevoEstado)) {
            throw new EstadoOrdenInvalidoException(
                    "Transición de estado inválida: " + estadoActual + " -> " + nuevoEstado);
        }

        if (nuevoEstado == EstadoOrden.PAGADA && !pagoService.estaOrdenTotalmentePagada(ordenId)) {
            throw new OrdenNoPagadaException(
                    "No se puede marcar como PAGADA una orden que no está totalmente pagada");
        }

        orden.setEstado(nuevoEstado);
        Orden ordenActualizada = ordenRepository.save(orden);
        return ordenMapper.toResponseDTO(ordenActualizada);
    }

    @Override
    @Transactional
    public OrdenResponseDTO cerrarOrden(Long ordenId) {
        // Delegate to the same transition validation + payment validation
        OrdenResponseDTO ordenCerrada = cambiarEstado(ordenId, EstadoOrden.PAGADA);

        // Release the table (defensive)
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new OrdenNotFoundException(
                        "Orden no encontrada con id: " + ordenId));

        Mesa mesa = orden.getMesa();
        if (mesa != null) {
            mesa.setEstado(EstadoMesa.DISPONIBLE);
            mesaRepository.save(mesa);
        }

        return ordenCerrada;
    }

    private boolean esTransicionValida(EstadoOrden actual, EstadoOrden nuevo) {
        if (actual == null || nuevo == null) {
            return false;
        }

        return switch (actual) {
            case PENDIENTE -> nuevo == EstadoOrden.EN_PREPARACION;
            case EN_PREPARACION -> nuevo == EstadoOrden.LISTA;
            case LISTA -> nuevo == EstadoOrden.ENTREGADA;
            case ENTREGADA -> nuevo == EstadoOrden.PAGADA;
            case PAGADA, CANCELADA -> false;
        };
    }

    private DetalleOrden crearDetalleConPrecioSnapshot(Orden orden, AgregarProductoRequestDTO request) {
        if (request.getProductoId() == null) {
            throw new InvalidStateException("El productoId es obligatorio");
        }
        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new InvalidStateException("La cantidad debe ser mayor a 0");
        }

        BigDecimal precioUnitario = obtenerPrecioProducto(request.getProductoId());
        if (precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidStateException("El precioUnitario debe ser mayor a 0");
        }

        BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(request.getCantidad()));

        return DetalleOrden.builder()
                .productoId(request.getProductoId())
                .cantidad(request.getCantidad())
                .precioUnitario(precioUnitario)
                .subtotal(subtotal)
                .orden(orden)
                .build();
    }

    private BigDecimal obtenerPrecioProducto(Long productoId) {
        ProductoDTO producto;
        try {
            producto = productoClient.obtenerProductoPorId(productoId);
        } catch (Exception ex) {
            throw new BusinessException("No se pudo obtener el producto desde Administrativo");
        }

        if (producto == null) {
            throw new InvalidStateException("Producto no encontrado con id: " + productoId);
        }
        if (producto.getActivo() == null || !producto.getActivo()) {
            throw new InvalidStateException("El producto no está activo con id: " + productoId);
        }

        BigDecimal precio = producto.getPrecio();
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidStateException("Precio inválido para el producto id: " + productoId);
        }

        return precio;
    }


}

