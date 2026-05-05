package com.smartbite.operativo.service.impl;

import com.smartbite.administrativo.dto.SucursalResponseDTO;
import com.smartbite.administrativo.dto.UsuarioResponseDTO;
import com.smartbite.operativo.client.ProductoClient;
import com.smartbite.operativo.client.SucursalClient;
import com.smartbite.operativo.client.UsuarioClient;
import com.smartbite.operativo.client.dto.ProductoDTO;
import com.smartbite.operativo.dto.detalle.AgregarProductoRequestDTO;
import com.smartbite.operativo.dto.detalle.DetalleOrdenResponseDTO;
import com.smartbite.operativo.dto.orden.CrearOrdenRequestDTO;
import com.smartbite.operativo.dto.orden.OrdenResumenDTO;
import com.smartbite.operativo.dto.orden.OrdenResponseDTO;
import com.smartbite.operativo.exception.*;
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
    private final UsuarioClient usuarioClient;
    private final SucursalClient sucursalClient;

    @Override
    @Transactional
    public OrdenResponseDTO crearOrden(CrearOrdenRequestDTO request) {
        validarUsuarioYSucursal(request.getUsuarioId(), request.getSucursalId());

        Mesa mesa = mesaRepository.findById(request.getMesaId())
                .orElseThrow(() -> new MesaNotFoundException(
                        "Mesa no encontrada con id: " + request.getMesaId()));

        if (!Objects.equals(mesa.getSucursalId(), request.getSucursalId())) {
            throw new InvalidStateException("La mesa no pertenece a la sucursal indicada");
        }

        Orden orden = Orden.builder()
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoOrden.PENDIENTE)
                .total(BigDecimal.ZERO)
                .mesa(mesa)
                .sucursalId(request.getSucursalId())
                .usuarioId(request.getUsuarioId())
                .build();

        // ✅ Agregar productos iniciales correctamente
        if (request.getProductos() != null && !request.getProductos().isEmpty()) {
            for (AgregarProductoRequestDTO producto : request.getProductos()) {
                DetalleOrden detalle = crearDetalleConPrecioSnapshot(orden, producto);
                orden.addDetalle(detalle); // 🔥 CORREGIDO
            }
        }

        recalcularTotalInterno(orden);

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

        orden.addDetalle(detalle); // 🔥 CORREGIDO

        detalleOrdenRepository.save(detalle);

        recalcularTotalInterno(orden);

        ordenRepository.save(orden);

        return detalleOrdenMapper.toResponseDTO(detalle);
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

        recalcularTotalInterno(orden);

        return ordenMapper.toResponseDTO(ordenRepository.save(orden));
    }

    @Override
    @Transactional
    public OrdenResponseDTO cambiarEstado(Long ordenId, EstadoOrden nuevoEstado) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new OrdenNotFoundException(
                        "Orden no encontrada con id: " + ordenId));

        if (!esTransicionValida(orden.getEstado(), nuevoEstado)) {
            throw new EstadoOrdenInvalidoException(
                    "Transición inválida: " + orden.getEstado() + " -> " + nuevoEstado);
        }

        if (nuevoEstado == EstadoOrden.PAGADA && !pagoService.estaOrdenTotalmentePagada(ordenId)) {
            throw new OrdenNoPagadaException(
                    "La orden no está totalmente pagada");
        }

        orden.setEstado(nuevoEstado);
        return ordenMapper.toResponseDTO(ordenRepository.save(orden));
    }

    @Override
    @Transactional
    public OrdenResponseDTO cerrarOrden(Long ordenId) {
        OrdenResponseDTO ordenCerrada = cambiarEstado(ordenId, EstadoOrden.PAGADA);

        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new OrdenNotFoundException(
                        "Orden no encontrada con id: " + ordenId));

        if (orden.getMesa() != null) {
            orden.getMesa().setEstado(EstadoMesa.DISPONIBLE);
            mesaRepository.save(orden.getMesa());
        }

        return ordenCerrada;
    }

    // ================= PRIVADOS =================

    private void recalcularTotalInterno(Orden orden) {
        BigDecimal total = orden.getDetalles()
                .stream()
                .map(d -> Objects.requireNonNullElse(d.getSubtotal(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        orden.setTotal(total);
    }

    private boolean esTransicionValida(EstadoOrden actual, EstadoOrden nuevo) {
        if (actual == null || nuevo == null) return false;

        return switch (actual) {
            case PENDIENTE -> nuevo == EstadoOrden.EN_PREPARACION;
            case EN_PREPARACION -> nuevo == EstadoOrden.LISTA;
            case LISTA -> nuevo == EstadoOrden.ENTREGADA;
            case ENTREGADA -> nuevo == EstadoOrden.PAGADA;
            default -> false;
        };
    }

    private DetalleOrden crearDetalleConPrecioSnapshot(Orden orden, AgregarProductoRequestDTO request) {
        if (request.getProductoId() == null) {
            throw new InvalidStateException("productoId obligatorio");
        }

        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new InvalidStateException("cantidad debe ser > 0");
        }

        BigDecimal precio = obtenerPrecioProducto(request.getProductoId());

        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(request.getCantidad()));

        return DetalleOrden.builder()
                .productoId(request.getProductoId())
                .cantidad(request.getCantidad())
                .precioUnitario(precio)
                .subtotal(subtotal)
                .orden(orden)
                .build();
    }

    private BigDecimal obtenerPrecioProducto(Long productoId) {
        try {
            ProductoDTO producto = productoClient.obtenerProductoPorId(productoId);

            if (producto == null) {
                throw new ResourceNotFoundException("Producto no encontrado con id: " + productoId);
            }

            if (Boolean.FALSE.equals(producto.getActivo())) {
                throw new InvalidStateException("Producto inactivo");
            }

            if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidStateException("Precio inválido");
            }

            return producto.getPrecio();

        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("Producto no encontrado con id: " + productoId);
        } catch (feign.FeignException e) {
            throw new BusinessException("Error con servicio administrativo");
        }
    }

    private void validarUsuarioYSucursal(Long usuarioId, Long sucursalId) {
        UsuarioResponseDTO usuario = usuarioClient.obtenerUsuarioPorId(usuarioId);
        if (usuario == null || Boolean.FALSE.equals(usuario.getActivo())) {
            throw new InvalidStateException("Usuario inválido");
        }

        SucursalResponseDTO sucursal = sucursalClient.obtenerSucursalPorId(sucursalId);
        if (sucursal == null || Boolean.FALSE.equals(sucursal.getActivo())) {
            throw new InvalidStateException("Sucursal inválida");
        }
    }
}