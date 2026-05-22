package com.smartbite.operativo.controller;

import com.smartbite.operativo.model.Mesa;
import com.smartbite.operativo.model.MetodoPago;
import com.smartbite.operativo.model.Orden;
import com.smartbite.operativo.model.Pago;
import com.smartbite.operativo.model.enums.EstadoMesa;
import com.smartbite.operativo.model.enums.EstadoOrden;
import com.smartbite.operativo.model.enums.EstadoPago;
import com.smartbite.operativo.model.enums.ProveedorPago;
import com.smartbite.operativo.repository.MesaRepository;
import com.smartbite.operativo.repository.MetodoPagoRepository;
import com.smartbite.operativo.repository.OrdenRepository;
import com.smartbite.operativo.repository.PagoRepository;
import com.smartbite.operativo.repository.VentaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "stripe.webhook-secret=whsec_test_secret",
                "stripe.secret-key=sk_test_dummy"
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StripeWebhookControllerIntegrationTest {

    private static final String WEBHOOK_URL = "/api/stripe/webhook";
    private static final String WEBHOOK_SECRET = "whsec_test_secret";
    private static final String DEFAULT_CURRENCY = "cop";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private MesaRepository mesaRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @AfterEach
    void tearDown() {
        ventaRepository.deleteAll();
        pagoRepository.deleteAll();
        ordenRepository.deleteAll();
        mesaRepository.deleteAll();
        metodoPagoRepository.deleteAll();
    }

    @Test
    void checkoutSessionCompleted_apruebaPago_yEsIdempotente() throws Exception {
        Pago pago = crearPagoConOrden(BigDecimal.valueOf(150.00));

        String payload = buildCheckoutSessionEvent(
                "evt_1",
                "cs_test_1",
                "pi_test_1",
                15000L,
                DEFAULT_CURRENCY,
                pago.getId(),
                pago.getOrden().getId()
        );

        String signature = buildStripeSignature(payload, WEBHOOK_SECRET);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        Pago actualizado = pagoRepository.findById(pago.getId()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoPago.APROBADO);
        assertThat(actualizado.getReferenciaTransaccion()).isEqualTo("pi_test_1");
        assertThat(actualizado.getSessionId()).isEqualTo("cs_test_1");

        Orden orden = ordenRepository.findById(pago.getOrden().getId()).orElseThrow();
        assertThat(orden.getMesa().getEstado()).isEqualTo(EstadoMesa.DISPONIBLE);
    }

    @Test
    void firmaInvalida_noProcesaPago() throws Exception {
        Pago pago = crearPagoConOrden(BigDecimal.valueOf(90.00));

        String payload = buildCheckoutSessionEvent(
                "evt_bad_sig",
                "cs_bad_sig",
                "pi_bad_sig",
                9000L,
                DEFAULT_CURRENCY,
                pago.getId(),
                pago.getOrden().getId()
        );

        String signature = "t=123,v1=deadbeef";

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        Pago actualizado = pagoRepository.findById(pago.getId()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoPago.PENDIENTE);
    }

    @Test
    void payloadCorrupto_conFirmaValida_devuelveError() throws Exception {
        String payload = "{\"id\":\"evt_corrupto\",\"object\":\"event\",\"type\":\"checkout.session.completed\",\"data\":{\"object\":}}";
        String signature = buildStripeSignature(payload, WEBHOOK_SECRET);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void montoInvalido_rechazaPago() throws Exception {
        Pago pago = crearPagoConOrden(BigDecimal.valueOf(120.00));

        String payload = buildCheckoutSessionEvent(
                "evt_monto",
                "cs_monto",
                "pi_monto",
                9999L,
                DEFAULT_CURRENCY,
                pago.getId(),
                pago.getOrden().getId()
        );

        String signature = buildStripeSignature(payload, WEBHOOK_SECRET);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        Pago actualizado = pagoRepository.findById(pago.getId()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
    }

    @Test
    void monedaInvalida_rechazaPago() throws Exception {
        Pago pago = crearPagoConOrden(BigDecimal.valueOf(80.00));

        String payload = buildCheckoutSessionEvent(
                "evt_currency",
                "cs_currency",
                "pi_currency",
                8000L,
                "usd",
                pago.getId(),
                pago.getOrden().getId()
        );

        String signature = buildStripeSignature(payload, WEBHOOK_SECRET);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        Pago actualizado = pagoRepository.findById(pago.getId()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
    }

    @Test
    void metadataFaltante_noProcesaPago() throws Exception {
        Pago pago = crearPagoConOrden(BigDecimal.valueOf(70.00));

        String payload = buildCheckoutSessionWithoutMetadataEvent(
                "evt_no_meta",
                "cs_no_meta",
                "pi_no_meta",
                7000L,
                DEFAULT_CURRENCY
        );
        String signature = buildStripeSignature(payload, WEBHOOK_SECRET);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        Pago actualizado = pagoRepository.findById(pago.getId()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoPago.PENDIENTE);
    }

    @Test
    void ordenIdInconsistente_noProcesaPago() throws Exception {
        Pago pago = crearPagoConOrden(BigDecimal.valueOf(60.00));

        String payload = buildCheckoutSessionEvent(
                "evt_orden_mismatch",
                "cs_orden_mismatch",
                "pi_orden_mismatch",
                6000L,
                DEFAULT_CURRENCY,
                pago.getId(),
                pago.getOrden().getId() + 999
        );

        String signature = buildStripeSignature(payload, WEBHOOK_SECRET);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        Pago actualizado = pagoRepository.findById(pago.getId()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoPago.PENDIENTE);
    }

    @Test
    void paymentFailed_actualizaEstado() throws Exception {
        Pago pago = crearPagoConOrden(BigDecimal.valueOf(110.00));

        String payload = buildPaymentIntentEvent(
                "evt_failed",
                "payment_intent.payment_failed",
                "pi_failed",
                pago.getId()
        );

        String signature = buildStripeSignature(payload, WEBHOOK_SECRET);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        Pago actualizado = pagoRepository.findById(pago.getId()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
    }

    @Test
    void paymentCanceled_actualizaEstado() throws Exception {
        Pago pago = crearPagoConOrden(BigDecimal.valueOf(55.00));

        String payload = buildPaymentIntentEvent(
                "evt_canceled",
                "payment_intent.canceled",
                "pi_canceled",
                pago.getId()
        );

        String signature = buildStripeSignature(payload, WEBHOOK_SECRET);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        Pago actualizado = pagoRepository.findById(pago.getId()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoPago.CANCELADO);
    }

    @Test
    void refund_actualizaEstado() throws Exception {
        Pago pago = crearPagoConOrden(BigDecimal.valueOf(200.00));
        pago.setReferenciaTransaccion("pi_refund");
        pagoRepository.save(pago);

        String payload = buildChargeRefundedEvent(
                "evt_refund",
                "ch_refund",
                "pi_refund"
        );

        String signature = buildStripeSignature(payload, WEBHOOK_SECRET);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        Pago actualizado = pagoRepository.findById(pago.getId()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoPago.REEMBOLSADO);
    }

    @Test
    void sessionExpired_cancelaPagoPendiente() throws Exception {
        Pago pago = crearPagoConOrden(BigDecimal.valueOf(45.00));

        String payload = buildCheckoutSessionExpiredEvent(
                "evt_expired",
                "cs_expired",
                pago.getId()
        );

        String signature = buildStripeSignature(payload, WEBHOOK_SECRET);

        mockMvc.perform(post(WEBHOOK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", signature)
                        .content(payload))
                .andExpect(status().isOk());

        Pago actualizado = pagoRepository.findById(pago.getId()).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoPago.CANCELADO);
    }

    private Pago crearPagoConOrden(BigDecimal total) {
        MetodoPago metodoPago = metodoPagoRepository.findByNombre("TARJETA")
                .orElseGet(() -> metodoPagoRepository.save(
                        MetodoPago.builder()
                                .nombre("TARJETA")
                                .build()
                ));

        Mesa mesa = mesaRepository.save(
                Mesa.builder()
                        .numero(1)
                        .capacidad(4)
                        .estado(EstadoMesa.OCUPADA)
                        .sucursalId(1L)
                        .build()
        );

        Orden orden = ordenRepository.save(
                Orden.builder()
                        .fechaCreacion(LocalDateTime.now())
                        .estado(EstadoOrden.PENDIENTE)
                        .total(total)
                        .mesa(mesa)
                        .sucursalId(1L)
                        .usuarioId(1L)
                        .build()
        );

        Pago pago = Pago.builder()
                .monto(total)
                .fechaPago(LocalDateTime.now())
                .estado(EstadoPago.PENDIENTE)
                .proveedorPago(ProveedorPago.STRIPE)
                .metodoPago(metodoPago)
                .orden(orden)
                .build();

        return pagoRepository.save(pago);
    }

    private String buildCheckoutSessionEvent(
            String eventId,
            String sessionId,
            String paymentIntentId,
            long amountTotal,
            String currency,
            Long pagoId,
            Long ordenId
    ) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(
                java.util.Map.of(
                        "id", eventId,
                        "object", "event",
                        "type", "checkout.session.completed",
                        "data", java.util.Map.of(
                                "object", java.util.Map.of(
                                        "id", sessionId,
                                        "object", "checkout.session",
                                        "payment_intent", paymentIntentId,
                                        "amount_total", amountTotal,
                                        "currency", currency,
                                        "metadata", java.util.Map.of(
                                                "pagoId", String.valueOf(pagoId),
                                                "ordenId", String.valueOf(ordenId)
                                        )
                                )
                        )
                )
        );
    }

    private String buildCheckoutSessionExpiredEvent(
            String eventId,
            String sessionId,
            Long pagoId
    ) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(
                java.util.Map.of(
                        "id", eventId,
                        "object", "event",
                        "type", "checkout.session.expired",
                        "data", java.util.Map.of(
                                "object", java.util.Map.of(
                                        "id", sessionId,
                                        "object", "checkout.session",
                                        "metadata", java.util.Map.of(
                                                "pagoId", String.valueOf(pagoId)
                                        )
                                )
                        )
                )
        );
    }

    private String buildPaymentIntentEvent(
            String eventId,
            String eventType,
            String paymentIntentId,
            Long pagoId
    ) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(
                java.util.Map.of(
                        "id", eventId,
                        "object", "event",
                        "type", eventType,
                        "data", java.util.Map.of(
                                "object", java.util.Map.of(
                                        "id", paymentIntentId,
                                        "object", "payment_intent",
                                        "metadata", java.util.Map.of(
                                                "pagoId", String.valueOf(pagoId)
                                        )
                                )
                        )
                )
        );
    }

    private String buildChargeRefundedEvent(
            String eventId,
            String chargeId,
            String paymentIntentId
    ) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(
                java.util.Map.of(
                        "id", eventId,
                        "object", "event",
                        "type", "charge.refunded",
                        "data", java.util.Map.of(
                                "object", java.util.Map.of(
                                        "id", chargeId,
                                        "object", "charge",
                                        "payment_intent", paymentIntentId
                                )
                        )
                )
        );
    }

    private String buildCheckoutSessionWithoutMetadataEvent(
            String eventId,
            String sessionId,
            String paymentIntentId,
            long amountTotal,
            String currency
    ) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(
                java.util.Map.of(
                        "id", eventId,
                        "object", "event",
                        "type", "checkout.session.completed",
                        "data", java.util.Map.of(
                                "object", java.util.Map.of(
                                        "id", sessionId,
                                        "object", "checkout.session",
                                        "payment_intent", paymentIntentId,
                                        "amount_total", amountTotal,
                                        "currency", currency
                                )
                        )
                )
        );
    }

    private String buildStripeSignature(
            String payload,
            String secret
    ) {
        long timestamp = Instant.now().getEpochSecond();
        String signedPayload = timestamp + "." + payload;

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            return "t=" + timestamp + ",v1=" + bytesToHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo firmar el payload", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
