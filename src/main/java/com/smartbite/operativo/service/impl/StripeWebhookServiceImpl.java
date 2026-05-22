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
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookServiceImpl
        implements StripeWebhookService {

    private static final String STRIPE_CURRENCY = "cop";

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

        procesarEvento(event);
    }

    void procesarEvento(Event event) {

        if (event == null) {
            return;
        }

        switch (event.getType()) {

            case "checkout.session.completed":
                procesarCheckoutCompletado(event);
                break;

            case "checkout.session.expired":
                procesarSesionExpirada(event);
                break;

            case "payment_intent.payment_failed":
                procesarPagoFallido(event);
                break;

            case "payment_intent.canceled":
                procesarPagoCancelado(event);
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

        if (session.getId() == null
                || session.getId().isBlank()) {

            log.error(
                    "Stripe sessionId inválido"
            );

            return;
        }

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

        String ordenIdStr =
                session.getMetadata()
                        .get("ordenId");

        if (pagoIdStr == null || ordenIdStr == null) {

            log.error(
                    "Metadata incompleta. sessionId={}, pagoId={}, ordenId={}",
                    session.getId(),
                    pagoIdStr,
                    ordenIdStr
            );

            return;
        }

        Long pagoId;
        Long ordenId;

        try {

            pagoId = Long.parseLong(
                    pagoIdStr
            );

            ordenId = Long.parseLong(
                    ordenIdStr
            );

        } catch (NumberFormatException e) {

            log.error(
                    "Metadata inválida. pagoId={}, ordenId={}",
                    pagoIdStr,
                    ordenIdStr
            );

            return;
        }

        Pago pago = pagoRepository.findByIdForUpdate(
                pagoId
        ).orElse(null);

        if (pago == null) {

            log.error(
                    "Pago no encontrado. pagoId={}",
                    pagoId
            );

            return;
        }

        Orden orden = pago.getOrden();

        if (orden == null
                || !orden.getId().equals(ordenId)) {

            log.error(
                    "Inconsistencia pago-orden. pagoId={}, ordenEsperada={}, ordenActual={}",
                    pagoId,
                    ordenId,
                    orden != null ? orden.getId() : null
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

        if (!validarMontoYMoneda(session, orden)) {

            log.error(
                    "Monto o moneda inválidos. sessionId={}, ordenId={}",
                    session.getId(),
                    orden.getId()
            );

            if (pago.getEstado() == EstadoPago.PENDIENTE) {
                pago.setEstado(EstadoPago.RECHAZADO);
                pagoRepository.save(pago);
            }

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
        PaymentIntent paymentIntent = obtenerPaymentIntent(event);

        if (paymentIntent == null) {
            return;
        }

        Optional<Pago> pagoOpt = obtenerPagoDesdePaymentIntent(paymentIntent);

        if (pagoOpt.isEmpty()) {

            log.warn(
                    "Pago no encontrado para payment_failed. paymentIntentId={}",
                    paymentIntent.getId()
            );

            return;
        }

        Pago pago = pagoRepository.findByIdForUpdate(
                pagoOpt.get().getId()
        ).orElse(null);

        if (pago == null) {
            return;
        }

        if (pago.getEstado() == EstadoPago.APROBADO) {
            log.warn(
                    "payment_failed recibido para pago aprobado. pagoId={}",
                    pago.getId()
            );
            return;
        }

        pago.setEstado(EstadoPago.RECHAZADO);

        if (pago.getReferenciaTransaccion() == null
                && paymentIntent.getId() != null) {
            pago.setReferenciaTransaccion(paymentIntent.getId());
        }

        pagoRepository.save(pago);

        log.warn(
                "Pago marcado como RECHAZADO. pagoId={}, paymentIntentId={}",
                pago.getId(),
                paymentIntent.getId()
        );
    }

    private void procesarPagoCancelado(
            Event event
    ) {

        PaymentIntent paymentIntent = obtenerPaymentIntent(event);

        if (paymentIntent == null) {
            return;
        }

        Optional<Pago> pagoOpt = obtenerPagoDesdePaymentIntent(paymentIntent);

        if (pagoOpt.isEmpty()) {

            log.warn(
                    "Pago no encontrado para cancelación. paymentIntentId={}",
                    paymentIntent.getId()
            );

            return;
        }

        Pago pago = pagoRepository.findByIdForUpdate(
                pagoOpt.get().getId()
        ).orElse(null);

        if (pago == null) {
            return;
        }

        if (pago.getEstado() == EstadoPago.APROBADO) {
            log.warn(
                    "payment_intent.canceled recibido para pago aprobado. pagoId={}",
                    pago.getId()
            );
            return;
        }

        pago.setEstado(EstadoPago.CANCELADO);

        if (pago.getReferenciaTransaccion() == null
                && paymentIntent.getId() != null) {
            pago.setReferenciaTransaccion(paymentIntent.getId());
        }

        pagoRepository.save(pago);

        log.warn(
                "Pago marcado como CANCELADO. pagoId={}, paymentIntentId={}",
                pago.getId(),
                paymentIntent.getId()
        );
    }

    private void procesarSesionExpirada(
            Event event
    ) {

        Session session = obtenerSession(event);

        if (session == null || session.getMetadata() == null) {
            return;
        }

        String pagoIdStr = session.getMetadata().get("pagoId");

        if (pagoIdStr == null) {
            return;
        }

        Long pagoId;
        try {
            pagoId = Long.parseLong(pagoIdStr);
        } catch (NumberFormatException e) {
            return;
        }

        Pago pago = pagoRepository.findByIdForUpdate(pagoId).orElse(null);

        if (pago == null) {
            return;
        }

        if (pago.getEstado() == EstadoPago.PENDIENTE) {
            pago.setEstado(EstadoPago.CANCELADO);
            pagoRepository.save(pago);
        }

        log.warn(
                "Sesión Stripe expirada. pagoId={}, sessionId={}",
                pagoId,
                session.getId()
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
        Charge charge = obtenerCharge(event);

        if (charge == null) {
            return;
        }

        String paymentIntentId =
                charge.getPaymentIntent() != null
                        ? charge.getPaymentIntent()
                        : charge.getId();

        if (paymentIntentId == null) {
            return;
        }

        Pago pago = pagoRepository.findByReferenciaTransaccion(
                paymentIntentId
        ).orElse(null);

        if (pago == null) {

            log.warn(
                    "Reembolso sin pago asociado. paymentIntentId={}",
                    paymentIntentId
            );

            return;
        }

        Pago pagoLocked = pagoRepository.findByIdForUpdate(pago.getId()).orElse(null);

        if (pagoLocked == null) {
            return;
        }

        if (pagoLocked.getEstado() != EstadoPago.REEMBOLSADO) {
            pagoLocked.setEstado(EstadoPago.REEMBOLSADO);
            pagoRepository.save(pagoLocked);
        }

        log.warn(
                "Pago marcado como REEMBOLSADO. pagoId={}, paymentIntentId={}",
                pagoLocked.getId(),
                paymentIntentId
        );
    }

    private boolean validarMontoYMoneda(
            Session session,
            Orden orden
    ) {

        if (session.getCurrency() == null
                || session.getAmountTotal() == null) {
            return false;
        }

        String currency = session.getCurrency().toLowerCase(Locale.ROOT);

        if (!STRIPE_CURRENCY.equals(currency)) {
            return false;
        }

        BigDecimal montoStripe =
                BigDecimal.valueOf(session.getAmountTotal())
                        .divide(BigDecimal.valueOf(100));

        return montoStripe.compareTo(orden.getTotal()) == 0;
    }

    private Optional<Pago> obtenerPagoDesdePaymentIntent(
            PaymentIntent paymentIntent
    ) {

        if (paymentIntent.getMetadata() != null) {
            String pagoIdStr = paymentIntent.getMetadata().get("pagoId");

            if (pagoIdStr != null) {
                try {
                    Long pagoId = Long.parseLong(pagoIdStr);
                    return pagoRepository.findById(pagoId);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }

        if (paymentIntent.getId() != null) {
            return pagoRepository.findByReferenciaTransaccion(paymentIntent.getId());
        }

        return Optional.empty();
    }

    private PaymentIntent obtenerPaymentIntent(
            Event event
    ) {

        EventDataObjectDeserializer deserializer =
                event.getDataObjectDeserializer();

        try {
            StripeObject stripeObject =
                    deserializer.deserializeUnsafe();

            return (PaymentIntent) stripeObject;

        } catch (Exception e) {

            log.error(
                    "Error deserializando PaymentIntent Stripe",
                    e
            );

            return null;
        }
    }

    private Charge obtenerCharge(
            Event event
    ) {

        EventDataObjectDeserializer deserializer =
                event.getDataObjectDeserializer();

        try {
            StripeObject stripeObject =
                    deserializer.deserializeUnsafe();

            return (Charge) stripeObject;

        } catch (Exception e) {

            log.error(
                    "Error deserializando Charge Stripe",
                    e
            );

            return null;
        }
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