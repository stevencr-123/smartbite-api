package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.dto.pago.CrearPagoRequestDTO;
import com.smartbite.operativo.dto.pago.PagoResponseDTO;
import com.smartbite.operativo.exception.*;
import com.smartbite.operativo.mapper.PagoMapper;
import com.smartbite.operativo.model.MetodoPago;
import com.smartbite.operativo.model.Orden;
import com.smartbite.operativo.model.Pago;
import com.smartbite.operativo.model.enums.EstadoOrden;
import com.smartbite.operativo.model.enums.EstadoPago;
import com.smartbite.operativo.repository.MetodoPagoRepository;
import com.smartbite.operativo.repository.OrdenRepository;
import com.smartbite.operativo.repository.PagoRepository;
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

        validarRequest(request);

        Orden orden = obtenerOrden(request.getOrdenId());

        validarEstadoOrden(orden);

        if (estaOrdenTotalmentePagada(orden.getId())) {

            throw new BusinessException(
                    "La orden ya está totalmente pagada"
            );
        }

        MetodoPago metodoPago = metodoPagoRepository
                .findById(request.getMetodoPagoId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Método de pago no encontrado"
                        )
                );

        validarMonto(
                orden,
                request.getMonto()
        );

        validarReferenciaUnica(
                request.getReferenciaTransaccion()
        );

        /*
         * =====================================================
         * PAGOS INTERNOS / MANUALES
         * =====================================================
         *
         * EFECTIVO
         * TRANSFERENCIA
         * TARJETA LOCAL
         *
         * No usan gateway externo.
         */
        Pago pago = Pago.builder()
                .monto(request.getMonto())
                .fechaPago(LocalDateTime.now())
                .estado(EstadoPago.APROBADO)

                .referenciaTransaccion(
                        request.getReferenciaTransaccion()
                )

                .orden(orden)
                .metodoPago(metodoPago)

                .proveedorPago(null)
                .sessionId(null)

                .build();

        Pago guardado = pagoRepository.save(pago);

        /*
         * =====================================================
         * ACTUALIZAR ESTADO OPERATIVO
         * =====================================================
         *
         * IMPORTANTE:
         * EstadoOrden ya NO maneja PAGADA.
         *
         * El cierre financiero ahora vive en:
         *
         * ✔ pagos
         * ✔ ventas
         * ✔ facturas
         *
         * La orden termina operativamente en ENTREGADA.
         */
        if (estaOrdenTotalmentePagada(
                orden.getId()
        )) {

            if (orden.getEstado()
                    != EstadoOrden.ENTREGADA) {

                orden.setEstado(
                        EstadoOrden.ENTREGADA
                );

                ordenRepository.save(
                        orden
                );
            }
        }

        return pagoMapper.toResponseDTO(
                guardado
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> obtenerPagosPorOrden(
            Long ordenId
    ) {

        if (!ordenRepository.existsById(
                ordenId
        )) {

            throw new OrdenNotFoundException(
                    "Orden no encontrada"
            );
        }

        return pagoRepository.findByOrdenId(
                        ordenId
                )
                .stream()
                .map(pagoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPagado(
            Long ordenId
    ) {

        return pagoRepository.findByOrdenIdAndEstado(
                        ordenId,
                        EstadoPago.APROBADO
                )
                .stream()
                .map(p ->
                        Objects.requireNonNullElse(
                                p.getMonto(),
                                BigDecimal.ZERO
                        )
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaOrdenTotalmentePagada(
            Long ordenId
    ) {

        Orden orden = obtenerOrden(
                ordenId
        );

        return calcularTotalPagado(
                ordenId
        ).compareTo(
                orden.getTotal()
        ) >= 0;
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO obtenerPagoPorId(
            Long pagoId
    ) {

        Pago pago = pagoRepository.findById(
                        pagoId
                )
                .orElseThrow(() ->
                        new PagoNotFoundException(
                                "Pago no encontrado"
                        )
                );

        return pagoMapper.toResponseDTO(
                pago
        );
    }

    /**
     * =========================================================
     * VALIDACIONES
     * =========================================================
     */

    private void validarRequest(
            CrearPagoRequestDTO request
    ) {

        if (request == null) {

            throw new BusinessException(
                    "Request inválido"
            );
        }

        if (request.getOrdenId() == null) {

            throw new BusinessException(
                    "ordenId obligatorio"
            );
        }

        if (request.getMetodoPagoId() == null) {

            throw new BusinessException(
                    "metodoPagoId obligatorio"
            );
        }

        if (request.getMonto() == null
                || request.getMonto()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Monto inválido"
            );
        }
    }

    private Orden obtenerOrden(
            Long ordenId
    ) {

        return ordenRepository.findById(
                        ordenId
                )
                .orElseThrow(() ->
                        new OrdenNotFoundException(
                                "Orden no encontrada"
                        )
                );
    }

    private void validarEstadoOrden(
            Orden orden
    ) {

        if (orden.getEstado()
                == EstadoOrden.CANCELADA) {

            throw new BusinessException(
                    "No se puede pagar una orden cancelada"
            );
        }

        /*
         * =====================================================
         * NUEVO FLUJO
         * =====================================================
         *
         * ENTREGADA ahora es el estado final operativo.
         *
         * Una orden ENTREGADA puede:
         *
         * ✔ tener pagos pendientes
         * ✔ recibir pagos parciales
         * ✔ cerrar venta/factura después
         *
         * Por eso NO bloqueamos ENTREGADA.
         */
    }

    private void validarMonto(
            Orden orden,
            BigDecimal monto
    ) {

        BigDecimal totalPagado =
                calcularTotalPagado(
                        orden.getId()
                );

        BigDecimal saldo =
                orden.getTotal()
                        .subtract(totalPagado);

        if (monto.compareTo(saldo) > 0) {

            throw new BusinessException(
                    "El monto excede el saldo pendiente"
            );
        }
    }

    private void validarReferenciaUnica(
            String referencia
    ) {

        if (referencia == null
                || referencia.isBlank()) {

            return;
        }

        if (pagoRepository.existsByReferenciaTransaccion(
                referencia
        )) {

            throw new BusinessException(
                    "La referencia ya existe"
            );
        }
    }
}