package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.config.StripeConfig;
import com.smartbite.operativo.model.Orden;
import com.smartbite.operativo.model.Pago;
import com.smartbite.operativo.model.enums.EstadoMesa;
import com.smartbite.operativo.model.enums.EstadoOrden;
import com.smartbite.operativo.model.enums.EstadoPago;
import com.smartbite.operativo.repository.OrdenRepository;
import com.smartbite.operativo.repository.PagoRepository;
import com.smartbite.operativo.service.StripeWebhookService;
import com.smartbite.operativo.service.VentaService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookServiceImpl
        implements StripeWebhookService {

    private final StripeConfig stripeConfig;
    private final OrdenRepository ordenRepository;
    private final PagoRepository pagoRepository;
    private final VentaService ventaService;

    @Override
    @Transactional
    public void procesarWebhook(
            String payload,
            String signature
    ) {

        Event event;

        try {

            event = Webhook.constructEvent(
                    payload,
                    signature,
                    stripeConfig.getWebhookSecret()
            );

        } catch (SignatureVerificationException e) {

            log.error(
                    "Firma webhook inválida",
                    e
            );

            return;
        }

        log.info(
                "Evento Stripe recibido: id={}, type={}",
                event.getId(),
                event.getType()
        );

        switch (event.getType()) {

            case "checkout.session.completed":
                procesarCheckoutCompletado(event);
                break;

            case "payment_intent.payment_failed":
                procesarPagoFallido(event);
                break;

            case "charge.refunded":
                procesarReembolso(event);
                break;

            default:
                log.info(
                        "Evento Stripe no manejado: {}",
                        event.getType()
                );
        }
    }

    /**
     * =========================================================
     * CHECKOUT EXITOSO
     * =========================================================
     */
    private void procesarCheckoutCompletado(
            Event event
    ) {

        Session session = obtenerSession(
                event
        );

        if (session == null) {
            return;
        }

        /*
         * =====================================================
         * VALIDAR SESSION ID
         * =====================================================
         */
        if (session.getId() == null
                || session.getId().isBlank()) {

            log.error(
                    "Stripe sessionId inválido"
            );

            return;
        }

        /*
         * =====================================================
         * VALIDAR METADATA
         * =====================================================
         */
        if (session.getMetadata() == null) {

            log.error(
                    "Session Stripe sin metadata. sessionId={}",
                    session.getId()
            );

            return;
        }

        String pagoIdStr =
                session.getMetadata()
                        .get("pagoId");

        if (pagoIdStr == null) {

            log.error(
                    "Metadata sin pagoId. sessionId={}",
                    session.getId()
            );

            return;
        }

        Long pagoId;

        try {

            pagoId = Long.parseLong(
                    pagoIdStr
            );

        } catch (NumberFormatException e) {

            log.error(
                    "pagoId inválido: {}",
                    pagoIdStr
            );

            return;
        }

        Pago pago = pagoRepository.findById(
                pagoId
        ).orElse(null);

        if (pago == null) {

            log.error(
                    "Pago no encontrado. pagoId={}",
                    pagoId
            );

            return;
        }

        /*
         * =====================================================
         * IDEMPOTENCIA 1
         * =====================================================
         *
         * Evita reprocesar el mismo pago.
         */
        if (pago.getEstado()
                == EstadoPago.APROBADO) {

            log.info(
                    "Webhook duplicado ignorado. pagoId={}",
                    pagoId
            );

            return;
        }

        String paymentIntentId =
                session.getPaymentIntent();

        if (paymentIntentId == null
                || paymentIntentId.isBlank()) {

            paymentIntentId =
                    session.getId();

            log.warn(
                    "PaymentIntent null. usando Session ID={}",
                    paymentIntentId
            );
        }

        /*
         * =====================================================
         * IDEMPOTENCIA 2
         * =====================================================
         *
         * Evita PaymentIntent duplicados
         * asociados a OTRO pago aprobado.
         */
        boolean referenciaYaExiste =
                pagoRepository
                        .findByReferenciaTransaccion(
                                paymentIntentId
                        )
                        .filter(p -> !p.getId().equals(
                                pago.getId()
                        ))
                        .isPresent();

        if (referenciaYaExiste) {

            log.warn(
                    "PaymentIntent ya procesado: {}",
                    paymentIntentId
            );

            return;
        }

        /*
         * =====================================================
         * IDEMPOTENCIA 3
         * =====================================================
         *
         * Evita Session IDs duplicados
         * asociados a OTRO pago.
         */
        boolean sessionYaExiste =
                pagoRepository
                        .findBySessionId(
                                session.getId()
                        )
                        .filter(p -> !p.getId().equals(
                                pago.getId()
                        ))
                        .isPresent();

        if (sessionYaExiste) {

            log.warn(
                    "Session Stripe ya procesada: {}",
                    session.getId()
            );

            return;
        }

        Orden orden = pago.getOrden();

        /*
         * =====================================================
         * VALIDAR ESTADO ACTUAL ORDEN
         * =====================================================
         */
        if (orden.getEstado() == EstadoOrden.CANCELADA) {

            log.error(
                    "No se puede aprobar pago de orden cancelada. ordenId={}",
                    orden.getId()
            );

            return;
        }

        /*
         * =====================================================
         * ACTUALIZAR PAGO
         * =====================================================
         */
        pago.setEstado(
                EstadoPago.APROBADO
        );

        pago.setReferenciaTransaccion(
                paymentIntentId
        );

        pago.setSessionId(
                session.getId()
        );

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
        }

        pagoRepository.save(
                pago
        );

        ordenRepository.save(
                orden
        );

        log.info(
                "Pago aprobado correctamente. ordenId={}, pagoId={}",
                orden.getId(),
                pagoId
        );

        /*
         * =====================================================
         * GENERAR VENTA + FACTURA
         * =====================================================
         *
         * El cierre financiero ya NO depende
         * de EstadoOrden.
         */
        try {

            ventaService.crearVentaDesdeOrden(
                    orden.getId(),
                    null
            );

            log.info(
                    "Venta generada automáticamente para orden {}",
                    orden.getId()
            );

        } catch (Exception e) {

            log.error(
                    "Error generando venta automática para orden {}",
                    orden.getId(),
                    e
            );
        }
    }

    /**
     * =========================================================
     * PAGO FALLIDO
     * =========================================================
     */
    private void procesarPagoFallido(
            Event event
    ) {

        log.warn(
                "Stripe notificó payment_failed. eventId={}",
                event.getId()
        );
    }

    /**
     * =========================================================
     * REEMBOLSO
     * =========================================================
     */
    private void procesarReembolso(
            Event event
    ) {

        log.warn(
                "Stripe notificó reembolso. eventId={}",
                event.getId()
        );
    }

    /**
     * =========================================================
     * DESERIALIZAR SESSION
     * =========================================================
     */
    private Session obtenerSession(
            Event event
    ) {

        EventDataObjectDeserializer deserializer =
                event.getDataObjectDeserializer();

        try {

            StripeObject stripeObject =
                    deserializer.deserializeUnsafe();

            return (Session) stripeObject;

        } catch (Exception e) {

            log.error(
                    "Error deserializando Session Stripe",
                    e
            );

            return null;
        }
    }
}