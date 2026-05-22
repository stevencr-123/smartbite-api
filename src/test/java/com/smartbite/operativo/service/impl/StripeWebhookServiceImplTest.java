package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.config.StripeConfig;
import com.smartbite.operativo.model.Orden;
import com.smartbite.operativo.model.Pago;
import com.smartbite.operativo.model.enums.EstadoOrden;
import com.smartbite.operativo.model.enums.EstadoPago;
import com.smartbite.operativo.repository.OrdenRepository;
import com.smartbite.operativo.repository.PagoRepository;
import com.smartbite.operativo.service.VentaService;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceImplTest {

    @Mock
    private StripeConfig stripeConfig;

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private VentaService ventaService;

    private StripeWebhookServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StripeWebhookServiceImpl(
                stripeConfig,
                ordenRepository,
                pagoRepository,
                ventaService
        );
    }

    @Test
    void checkoutCompletado_apruebaPago_sinCambiarEstadoOrden() {
        Session session = sessionBasica();

        Orden orden = ordenBasica(EstadoOrden.EN_PREPARACION);
        Pago pago = pagoBasico(orden, EstadoPago.PENDIENTE);

        when(pagoRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(pago));
        when(pagoRepository.findByReferenciaTransaccion(anyString()))
                .thenReturn(Optional.empty());
        when(pagoRepository.findBySessionId(anyString()))
                .thenReturn(Optional.empty());

        Event event = mockEvent("checkout.session.completed", session);
        service.procesarEvento(event);

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        assertThat(pagoCaptor.getValue().getEstado()).isEqualTo(EstadoPago.APROBADO);

        ArgumentCaptor<Orden> ordenCaptor = ArgumentCaptor.forClass(Orden.class);
        verify(ordenRepository).save(ordenCaptor.capture());
        assertThat(ordenCaptor.getValue().getEstado()).isEqualTo(EstadoOrden.EN_PREPARACION);

        verify(ventaService).crearVentaDesdeOrden(orden.getId(), null);
    }

    @Test
    void checkoutCompletado_metadataInvalida_noProcesa() {
        Session session = sessionBasica();
        session.setMetadata(new HashMap<>());

        Event event = mockEvent("checkout.session.completed", session);
        service.procesarEvento(event);

        verify(pagoRepository, never()).findByIdForUpdate(anyLong());
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void checkoutCompletado_montoInvalido_rechazaPago() {
        Session session = sessionBasica();
        session.setAmountTotal(9000L);

        Orden orden = ordenBasica(EstadoOrden.PENDIENTE);
        Pago pago = pagoBasico(orden, EstadoPago.PENDIENTE);

        when(pagoRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(pago));
        when(pagoRepository.findByReferenciaTransaccion(anyString()))
                .thenReturn(Optional.empty());
        when(pagoRepository.findBySessionId(anyString()))
                .thenReturn(Optional.empty());

        Event event = mockEvent("checkout.session.completed", session);
        service.procesarEvento(event);

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        assertThat(pagoCaptor.getValue().getEstado()).isEqualTo(EstadoPago.RECHAZADO);
    }

    @Test
    void checkoutCompletado_monedaInvalida_rechazaPago() {
        Session session = sessionBasica();
        session.setCurrency("usd");

        Orden orden = ordenBasica(EstadoOrden.PENDIENTE);
        Pago pago = pagoBasico(orden, EstadoPago.PENDIENTE);

        when(pagoRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(pago));
        when(pagoRepository.findByReferenciaTransaccion(anyString()))
                .thenReturn(Optional.empty());
        when(pagoRepository.findBySessionId(anyString()))
                .thenReturn(Optional.empty());

        Event event = mockEvent("checkout.session.completed", session);
        service.procesarEvento(event);

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        assertThat(pagoCaptor.getValue().getEstado()).isEqualTo(EstadoPago.RECHAZADO);
    }

    @Test
    void checkoutCompletado_duplicado_porPaymentIntent_noProcesa() {
        Session session = sessionBasica();

        Orden orden = ordenBasica(EstadoOrden.PENDIENTE);
        Pago pago = pagoBasico(orden, EstadoPago.PENDIENTE);
        Pago pagoExistente = pagoBasico(orden, EstadoPago.APROBADO);
        pagoExistente.setId(99L);

        when(pagoRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(pago));
        when(pagoRepository.findByReferenciaTransaccion(anyString()))
                .thenReturn(Optional.of(pagoExistente));

        Event event = mockEvent("checkout.session.completed", session);
        service.procesarEvento(event);

        verify(pagoRepository, never()).save(any(Pago.class));
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    void checkoutCompletado_webhookReplay_pagoYaAprobado_noProcesa() {
        Session session = sessionBasica();

        Orden orden = ordenBasica(EstadoOrden.PENDIENTE);
        Pago pago = pagoBasico(orden, EstadoPago.APROBADO);

        when(pagoRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(pago));

        Event event = mockEvent("checkout.session.completed", session);
        service.procesarEvento(event);

        verify(pagoRepository, never()).save(any(Pago.class));
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    void checkoutSesionExpirada_cancelaPagoPendiente() {
        Session session = sessionBasica();

        Orden orden = ordenBasica(EstadoOrden.PENDIENTE);
        Pago pago = pagoBasico(orden, EstadoPago.PENDIENTE);

        when(pagoRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(pago));

        Event event = mockEvent("checkout.session.expired", session);
        service.procesarEvento(event);

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        assertThat(pagoCaptor.getValue().getEstado()).isEqualTo(EstadoPago.CANCELADO);
    }

    @Test
    void paymentFailed_rechazaPago() {
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId("pi_123");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("pagoId", "1");
        paymentIntent.setMetadata(metadata);

        Orden orden = ordenBasica(EstadoOrden.PENDIENTE);
        Pago pago = pagoBasico(orden, EstadoPago.PENDIENTE);

        when(pagoRepository.findById(1L))
                .thenReturn(Optional.of(pago));
        when(pagoRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(pago));

        Event event = mockEvent("payment_intent.payment_failed", paymentIntent);
        service.procesarEvento(event);

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        assertThat(pagoCaptor.getValue().getEstado()).isEqualTo(EstadoPago.RECHAZADO);
        assertThat(pagoCaptor.getValue().getReferenciaTransaccion()).isEqualTo("pi_123");
    }

    @Test
    void refund_marcaPagoReembolsado() {
        Charge charge = new Charge();
        charge.setPaymentIntent("pi_123");

        Orden orden = ordenBasica(EstadoOrden.PENDIENTE);
        Pago pago = pagoBasico(orden, EstadoPago.APROBADO);

        when(pagoRepository.findByReferenciaTransaccion("pi_123"))
                .thenReturn(Optional.of(pago));
        when(pagoRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(pago));

        Event event = mockEvent("charge.refunded", charge);
        service.procesarEvento(event);

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pagoCaptor.capture());
        assertThat(pagoCaptor.getValue().getEstado()).isEqualTo(EstadoPago.REEMBOLSADO);
    }

    private Event mockEvent(String type, StripeObject stripeObject) {
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(type);

        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        try {
            when(deserializer.deserializeUnsafe()).thenReturn(stripeObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return event;
    }

    private Session sessionBasica() {
        Session session = new Session();
        session.setId("cs_123");
        session.setCurrency("cop");
        session.setAmountTotal(10000L);
        session.setPaymentIntent("pi_123");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("pagoId", "1");
        metadata.put("ordenId", "2");
        session.setMetadata(metadata);

        return session;
    }

    private Orden ordenBasica(EstadoOrden estadoOrden) {
        Orden orden = new Orden();
        orden.setId(2L);
        orden.setEstado(estadoOrden);
        orden.setTotal(BigDecimal.valueOf(100));
        return orden;
    }

    private Pago pagoBasico(Orden orden, EstadoPago estadoPago) {
        Pago pago = new Pago();
        pago.setId(1L);
        pago.setOrden(orden);
        pago.setEstado(estadoPago);
        return pago;
    }
}

