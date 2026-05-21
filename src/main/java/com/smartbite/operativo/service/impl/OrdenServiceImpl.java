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
import com.smartbite.operativo.dto.orden.OrdenResponseDTO;
import com.smartbite.operativo.dto.orden.OrdenResumenDTO;
import com.smartbite.operativo.exception.*;
import com.smartbite.operativo.mapper.DetalleOrdenMapper;
import com.smartbite.operativo.mapper.OrdenMapper;
import com.smartbite.operativo.model.Cliente;
import com.smartbite.operativo.model.DetalleOrden;
import com.smartbite.operativo.model.Mesa;
import com.smartbite.operativo.model.Orden;
import com.smartbite.operativo.model.enums.EstadoMesa;
import com.smartbite.operativo.model.enums.EstadoOrden;
import com.smartbite.operativo.repository.ClienteRepository;
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
    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public OrdenResponseDTO crearOrden(
            CrearOrdenRequestDTO request
    ) {

        validarUsuarioYSucursal(
                request.getUsuarioId(),
                request.getSucursalId()
        );


        Cliente cliente = null;

        /*
         * =====================================================
         * CLIENTE OPCIONAL
         * =====================================================
         */
        if (request.getClienteId() != null) {

            cliente = clienteRepository.findById(
                    request.getClienteId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Cliente no encontrado con id: "
                                    + request.getClienteId()
                    )
            );

            if (Boolean.FALSE.equals(
                    cliente.getActivo()
            )) {

                throw new InvalidStateException(
                        "El cliente se encuentra inactivo"
                );
            }
        }

        Mesa mesa = mesaRepository.findById(
                request.getMesaId()
        ).orElseThrow(() ->
                new MesaNotFoundException(
                        "Mesa no encontrada con id: "
                                + request.getMesaId()
                )
        );

        if (Boolean.FALSE.equals(
                mesa.getActiva()
        )) {

            throw new InvalidStateException(
                    "La mesa se encuentra inactiva"
            );
        }

        if (!Objects.equals(
                mesa.getSucursalId(),
                request.getSucursalId()
        )) {

            throw new InvalidStateException(
                    "La mesa no pertenece a la sucursal indicada"
            );
        }

        List<EstadoOrden> estadosActivos = List.of(
                EstadoOrden.PENDIENTE,
                EstadoOrden.EN_PREPARACION,
                EstadoOrden.LISTA
        );

        boolean existeOrdenActiva =
                ordenRepository
                        .findByMesaIdAndEstadoIn(
                                mesa.getId(),
                                estadosActivos
                        )
                        .isPresent();

        if (existeOrdenActiva) {

            throw new InvalidStateException(
                    "La mesa ya tiene una orden activa"
            );
        }

        Orden orden = Orden.builder()
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoOrden.PENDIENTE)
                .total(BigDecimal.ZERO)
                .mesa(mesa)
                .cliente(cliente)
                .sucursalId(request.getSucursalId())
                .usuarioId(request.getUsuarioId())
                .build();

        /*
         * =====================================================
         * AGREGAR PRODUCTOS INICIALES
         * =====================================================
         */
        if (request.getProductos() != null
                && !request.getProductos().isEmpty()) {

            for (AgregarProductoRequestDTO producto
                    : request.getProductos()) {

                DetalleOrden detalle =
                        crearDetalleConPrecioSnapshot(
                                orden,
                                producto
                        );

                orden.addDetalle(
                        detalle
                );
            }
        }

        recalcularTotalInterno(
                orden
        );

        mesa.setEstado(
                EstadoMesa.OCUPADA
        );

        mesaRepository.save(
                mesa
        );

        Orden ordenGuardada =
                ordenRepository.save(
                        orden
                );

        return construirRespuestaOrden(
                ordenGuardada
        );
    }

    @Override
    @Transactional
    public DetalleOrdenResponseDTO agregarProducto(
            Long ordenId,
            AgregarProductoRequestDTO request
    ) {

        Orden orden = ordenRepository.findById(
                ordenId
        ).orElseThrow(() ->
                new OrdenNotFoundException(
                        "Orden no encontrada con id: "
                                + ordenId
                )
        );

        /*
         * =====================================================
         * VALIDAR ESTADO OPERATIVO
         * =====================================================
         */
        if (orden.getEstado() == EstadoOrden.CANCELADA
                || orden.getEstado() == EstadoOrden.ENTREGADA) {

            throw new InvalidStateException(
                    "No se pueden agregar productos a una orden en estado: "
                            + orden.getEstado()
            );
        }

        DetalleOrden detalle =
                crearDetalleConPrecioSnapshot(
                        orden,
                        request
                );

        orden.addDetalle(
                detalle
        );

        detalleOrdenRepository.save(
                detalle
        );

        recalcularTotalInterno(
                orden
        );

        ordenRepository.save(
                orden
        );

        return detalleOrdenMapper.toResponseDTO(
                detalle
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenResponseDTO obtenerOrdenPorId(
            Long ordenId
    ) {

        Orden orden = ordenRepository.findById(
                ordenId
        ).orElseThrow(() ->
                new OrdenNotFoundException(
                        "Orden no encontrada con id: "
                                + ordenId
                )
        );

        return construirRespuestaOrden(
                orden
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenResumenDTO> obtenerOrdenesActivas() {

        List<EstadoOrden> estadosActivos = List.of(
                EstadoOrden.PENDIENTE,
                EstadoOrden.EN_PREPARACION,
                EstadoOrden.LISTA
        );

        return ordenRepository.findByEstadoIn(
                        estadosActivos
                )
                .stream()
                .map(ordenMapper::toResumenDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrdenResponseDTO recalcularTotal(
            Long ordenId
    ) {

        Orden orden = ordenRepository.findById(
                ordenId
        ).orElseThrow(() ->
                new OrdenNotFoundException(
                        "Orden no encontrada con id: "
                                + ordenId
                )
        );

        recalcularTotalInterno(
                orden
        );

        Orden actualizada =
                ordenRepository.save(
                        orden
                );

        return construirRespuestaOrden(
                actualizada
        );
    }

    @Override
    @Transactional
    public OrdenResponseDTO cambiarEstado(
            Long ordenId,
            EstadoOrden nuevoEstado
    ) {

        Orden orden = ordenRepository.findById(
                ordenId
        ).orElseThrow(() ->
                new OrdenNotFoundException(
                        "Orden no encontrada con id: "
                                + ordenId
                )
        );

        if (!esTransicionValida(
                orden.getEstado(),
                nuevoEstado
        )) {

            throw new EstadoOrdenInvalidoException(
                    "Transición inválida: "
                            + orden.getEstado()
                            + " -> "
                            + nuevoEstado
            );
        }

        orden.setEstado(
                nuevoEstado
        );

        /*
         * =====================================================
         * TIMESTAMP DE ENTREGA
         * =====================================================
         */
        if (nuevoEstado == EstadoOrden.ENTREGADA) {

            orden.setFechaEntrega(
                    LocalDateTime.now()
            );
        }

        Orden actualizada =
                ordenRepository.save(
                        orden
                );

        return construirRespuestaOrden(
                actualizada
        );
    }

    @Override
    @Transactional
    public OrdenResponseDTO cerrarOrden(
            Long ordenId
    ) {

        Orden orden = ordenRepository.findById(
                ordenId
        ).orElseThrow(() ->
                new OrdenNotFoundException(
                        "Orden no encontrada con id: "
                                + ordenId
                )
        );

        /*
         * =====================================================
         * VALIDAR FLUJO OPERATIVO
         * =====================================================
         */
        if (orden.getEstado()
                != EstadoOrden.ENTREGADA) {

            throw new InvalidStateException(
                    "La orden debe estar ENTREGADA para cerrarse"
            );
        }

        /*
         * =====================================================
         * VALIDAR FLUJO FINANCIERO
         * =====================================================
         */
        if (!pagoService.estaOrdenTotalmentePagada(
                ordenId
        )) {

            throw new OrdenNoPagadaException(
                    "La orden no está totalmente pagada"
            );
        }

        /*
         * =====================================================
         * LIBERAR MESA
         * =====================================================
         */
        if (orden.getMesa() != null) {

            orden.getMesa()
                    .setEstado(
                            EstadoMesa.DISPONIBLE
                    );

            mesaRepository.save(
                    orden.getMesa()
            );
        }

        Orden actualizada =
                ordenRepository.save(
                        orden
                );

        return construirRespuestaOrden(
                actualizada
        );
    }

    // =========================================================
    // PRIVADOS
    // =========================================================

    private OrdenResponseDTO construirRespuestaOrden(
            Orden orden
    ) {

        OrdenResponseDTO response =
                ordenMapper.toResponseDTO(
                        orden
                );

        try {

            UsuarioResponseDTO usuario =
                    usuarioClient.obtenerUsuarioPorId(
                            orden.getUsuarioId()
                    );

            if (usuario != null) {

                response.setNombreUsuario(
                        usuario.getNombre()
                );
            }

        } catch (Exception ignored) {
        }

        return response;
    }

    private void recalcularTotalInterno(
            Orden orden
    ) {

        BigDecimal total = orden.getDetalles()
                .stream()
                .map(d ->
                        Objects.requireNonNullElse(
                                d.getSubtotal(),
                                BigDecimal.ZERO
                        )
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        orden.setTotal(
                total
        );
    }

    private boolean esTransicionValida(
            EstadoOrden actual,
            EstadoOrden nuevo
    ) {

        if (actual == null || nuevo == null) {
            return false;
        }

        return switch (actual) {

            case PENDIENTE ->
                    nuevo == EstadoOrden.EN_PREPARACION
                            || nuevo == EstadoOrden.CANCELADA;

            case EN_PREPARACION ->
                    nuevo == EstadoOrden.LISTA
                            || nuevo == EstadoOrden.CANCELADA;

            case LISTA ->
                    nuevo == EstadoOrden.ENTREGADA
                            || nuevo == EstadoOrden.CANCELADA;

            case ENTREGADA ->
                    false;

            case CANCELADA ->
                    false;
        };
    }

    private DetalleOrden crearDetalleConPrecioSnapshot(
            Orden orden,
            AgregarProductoRequestDTO request
    ) {

        if (request.getProductoId() == null) {

            throw new InvalidStateException(
                    "productoId obligatorio"
            );
        }

        if (request.getCantidad() == null
                || request.getCantidad() <= 0) {

            throw new InvalidStateException(
                    "cantidad debe ser > 0"
            );
        }

        BigDecimal precio =
                obtenerPrecioProducto(
                        request.getProductoId()
                );

        BigDecimal subtotal =
                precio.multiply(
                        BigDecimal.valueOf(
                                request.getCantidad()
                        )
                );

        return DetalleOrden.builder()
                .productoId(request.getProductoId())
                .cantidad(request.getCantidad())
                .precioUnitario(precio)
                .subtotal(subtotal)
                .orden(orden)
                .build();
    }

    private BigDecimal obtenerPrecioProducto(
            Long productoId
    ) {

        try {

            ProductoDTO producto =
                    productoClient.obtenerProductoPorId(
                            productoId
                    );

            if (producto == null) {

                throw new ResourceNotFoundException(
                        "Producto no encontrado con id: "
                                + productoId
                );
            }

            if (Boolean.FALSE.equals(
                    producto.getActivo()
            )) {

                throw new InvalidStateException(
                        "Producto inactivo"
                );
            }

            if (producto.getPrecio() == null
                    || producto.getPrecio()
                    .compareTo(BigDecimal.ZERO) <= 0) {

                throw new InvalidStateException(
                        "Precio inválido"
                );
            }

            return producto.getPrecio();

        } catch (feign.FeignException.NotFound e) {

            throw new ResourceNotFoundException(
                    "Producto no encontrado con id: "
                            + productoId
            );

        } catch (feign.FeignException e) {

            throw new BusinessException(
                    "Error con servicio administrativo"
            );
        }
    }

    private void validarUsuarioYSucursal(
            Long usuarioId,
            Long sucursalId
    ) {

        UsuarioResponseDTO usuario =
                usuarioClient.obtenerUsuarioPorId(
                        usuarioId
                );

        if (usuario == null
                || Boolean.FALSE.equals(
                usuario.getActivo()
        )) {

            throw new InvalidStateException(
                    "Usuario inválido"
            );
        }

        SucursalResponseDTO sucursal =
                sucursalClient.obtenerSucursalPorId(
                        sucursalId
                );

        if (sucursal == null
                || Boolean.FALSE.equals(
                sucursal.getActivo()
        )) {

            throw new InvalidStateException(
                    "Sucursal inválida"
            );
        }
    }
}