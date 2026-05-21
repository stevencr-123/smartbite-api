package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.dto.stripe.StripeCheckoutResponseDTO;
import com.smartbite.operativo.exception.BusinessException;
import com.smartbite.operativo.exception.OrdenNotFoundException;
import com.smartbite.operativo.model.MetodoPago;
import com.smartbite.operativo.model.Orden;
import com.smartbite.operativo.model.Pago;
import com.smartbite.operativo.model.enums.EstadoOrden;
import com.smartbite.operativo.model.enums.EstadoPago;
import com.smartbite.operativo.model.enums.ProveedorPago;
import com.smartbite.operativo.repository.MetodoPagoRepository;
import com.smartbite.operativo.repository.OrdenRepository;
import com.smartbite.operativo.repository.PagoRepository;
import com.smartbite.operativo.service.PagoService;
import com.smartbite.operativo.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeServiceImpl implements StripeService {

    private final OrdenRepository ordenRepository;
    private final PagoRepository pagoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PagoService pagoService;

    @Override
    @Transactional
    public StripeCheckoutResponseDTO crearCheckoutSession(
            Long ordenId
    ) {

        if (ordenId == null) {

            throw new BusinessException(
                    "ordenId es obligatorio"
            );
        }

        Orden orden = ordenRepository.findById(
                        ordenId
                )
                .orElseThrow(() ->
                        new OrdenNotFoundException(
                                "Orden no encontrada"
                        )
                );

        validarOrdenDisponibleParaPago(
                orden
        );

        /*
         * =====================================================
         * MÉTODO DE PAGO
         * =====================================================
         *
         * Stripe procesa pagos con TARJETA.
         */
        MetodoPago metodoTarjeta = metodoPagoRepository
                .findByNombre("TARJETA")
                .orElseThrow(() ->
                        new BusinessException(
                                "Método TARJETA no configurado"
                        )
                );

        /*
         * =====================================================
         * REINTENTOS LIMPIOS
         * =====================================================
         *
         * SOLO reutilizar pagos:
         *
         * - PENDIENTES
         * - SIN sessionId
         *
         * Si ya existe sessionId:
         * -> crear nuevo pago
         */
        Pago pagoPendiente =
                obtenerOCrearPagoPendiente(
                        orden,
                        metodoTarjeta
                );

        try {

            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData
                            .builder()
                            .setName(
                                    "Orden SmartBite #" + orden.getId()
                            )
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData
                            .builder()
                            .setCurrency("cop")
                            .setUnitAmount(
                                    orden.getTotal()
                                            .multiply(java.math.BigDecimal.valueOf(100))
                                            .longValue()
                            )
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem
                            .builder()
                            .setQuantity(1L)
                            .setPriceData(priceData)
                            .build();

            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(
                                    SessionCreateParams.Mode.PAYMENT
                            )

                            .setSuccessUrl(
                                    "http://localhost:3000/pago-exitoso"
                            )

                            .setCancelUrl(
                                    "http://localhost:3000/pago-cancelado"
                            )

                            /*
                             * =================================================
                             * METADATA CRÍTICA
                             * =================================================
                             */
                            .putMetadata(
                                    "ordenId",
                                    orden.getId().toString()
                            )

                            .putMetadata(
                                    "pagoId",
                                    pagoPendiente.getId().toString()
                            )

                            .addLineItem(lineItem)
                            .build();

            Session session =
                    Session.create(params);

            /*
             * =====================================================
             * PERSISTIR SESSION STRIPE
             * =====================================================
             */
            pagoPendiente.setSessionId(
                    session.getId()
            );

            pagoRepository.save(
                    pagoPendiente
            );

            log.info(
                    "Sesión Stripe creada correctamente. ordenId={}, pagoId={}, sessionId={}",
                    orden.getId(),
                    pagoPendiente.getId(),
                    session.getId()
            );

            return StripeCheckoutResponseDTO
                    .builder()
                    .sessionId(
                            session.getId()
                    )
                    .checkoutUrl(
                            session.getUrl()
                    )
                    .build();

        } catch (StripeException e) {

            log.error(
                    "Error creando sesión Stripe para orden {}",
                    ordenId,
                    e
            );

            throw new BusinessException(
                    "Error creando sesión de pago Stripe"
            );
        }
    }

    /**
     * =========================================================
     * VALIDAR ORDEN
     * =========================================================
     */
    private void validarOrdenDisponibleParaPago(
            Orden orden
    ) {

        /*
         * =====================================================
         * ORDEN CANCELADA
         * =====================================================
         */
        if (orden.getEstado()
                == EstadoOrden.CANCELADA) {

            throw new BusinessException(
                    "No se puede pagar una orden cancelada"
            );
        }

        /*
         * =====================================================
         * NUEVO FLUJO OPERATIVO
         * =====================================================
         *
         * EstadoOrden ya NO maneja PAGADA.
         *
         * ENTREGADA representa:
         * - cierre operativo
         * - entrega al cliente
         *
         * Pero el cierre financiero se valida
         * EXCLUSIVAMENTE desde pagos.
         */

        /*
         * =====================================================
         * VALIDACIÓN FINANCIERA REAL
         * =====================================================
         */
        if (pagoService.estaOrdenTotalmentePagada(
                orden.getId()
        )) {

            throw new BusinessException(
                    "La orden ya se encuentra totalmente pagada"
            );
        }
    }

    /**
     * =========================================================
     * REINTENTOS LIMPIOS
     * =========================================================
     */
    private Pago obtenerOCrearPagoPendiente(
            Orden orden,
            MetodoPago metodoTarjeta
    ) {

        Optional<Pago> pagoPendienteExistente =
                pagoRepository
                        .findFirstByOrdenIdAndEstadoOrderByIdDesc(
                                orden.getId(),
                                EstadoPago.PENDIENTE
                        );

        /*
         * =====================================================
         * REUTILIZAR SOLO SI:
         * - NO tiene session Stripe todavía
         * =====================================================
         */
        if (pagoPendienteExistente.isPresent()) {

            Pago pagoExistente =
                    pagoPendienteExistente.get();

            if (pagoExistente.getSessionId() == null) {

                log.info(
                        "Reutilizando pago pendiente sin sessionId. pagoId={}",
                        pagoExistente.getId()
                );

                return pagoExistente;
            }

            log.info(
                    "Pago pendiente anterior ya tenía sessionId. Creando nuevo intento. pagoId={}",
                    pagoExistente.getId()
            );
        }

        /*
         * =====================================================
         * NUEVO INTENTO LIMPIO
         * =====================================================
         */
        Pago nuevoPagoPendiente = Pago.builder()
                .monto(
                        orden.getTotal()
                )
                .fechaPago(
                        LocalDateTime.now()
                )
                .estado(
                        EstadoPago.PENDIENTE
                )
                .orden(
                        orden
                )
                .metodoPago(
                        metodoTarjeta
                )
                .proveedorPago(
                        ProveedorPago.STRIPE
                )
                .sessionId(null)
                .referenciaTransaccion(null)
                .build();

        Pago pagoGuardado =
                pagoRepository.save(
                        nuevoPagoPendiente
                );

        log.info(
                "Nuevo pago pendiente creado. pagoId={}",
                pagoGuardado.getId()
        );

        return pagoGuardado;
    }
}