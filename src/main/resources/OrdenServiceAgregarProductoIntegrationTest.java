package com.smartbite.operativo.service;

import com.smartbite.App;
import com.smartbite.operativo.client.ProductoClient;
import com.smartbite.operativo.client.dto.ProductoDTO;
import com.smartbite.operativo.dto.detalle.AgregarProductoRequestDTO;
import com.smartbite.operativo.dto.detalle.DetalleOrdenResponseDTO;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = App.class)
@ActiveProfiles("test")
class OrdenServiceAgregarProductoIntegrationTest {

    @Autowired
    private OrdenService ordenService;

    @MockBean
    private ProductoClient productoClient;

    @MockBean
    private OrdenRepository ordenRepository;

    @MockBean
    private DetalleOrdenRepository detalleOrdenRepository;

    @MockBean
    private MesaRepository mesaRepository;

    @MockBean
    private PagoService pagoService;

    @MockBean
    private OrdenMapper ordenMapper;

    @MockBean
    private DetalleOrdenMapper detalleOrdenMapper;

    @Test
    void agregarProducto_usaPrecioRemoto_yCalculaSubtotalSnapshot() {
        Long ordenId = 100L;
        Long productoId = 1L;
        int cantidad = 2;

        Mesa mesa = Mesa.builder()
                .id(10L)
                .numero(1)
                .capacidad(4)
                .estado(EstadoMesa.OCUPADA)
                .activa(true)
                .sucursalId(99L)
                .build();

        Orden orden = Orden.builder()
                .id(ordenId)
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoOrden.PENDIENTE)
                .mesa(mesa)
                .sucursalId(99L)
                .usuarioId(77L)
                .build();

        when(ordenRepository.findById(ordenId)).thenReturn(Optional.of(orden));

        ProductoDTO producto = new ProductoDTO();
        producto.setId(productoId);
        producto.setNombre("Producto");
        producto.setActivo(true);
        producto.setPrecio(new BigDecimal("25000"));

        when(productoClient.obtenerProductoPorId(productoId)).thenReturn(producto);

        ArgumentCaptor<DetalleOrden> detalleCaptor = ArgumentCaptor.forClass(DetalleOrden.class);
        when(detalleOrdenRepository.save(detalleCaptor.capture())).thenAnswer(inv -> {
            DetalleOrden d = inv.getArgument(0);
            d.setId(555L);
            return d;
        });

        when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));

        when(detalleOrdenMapper.toResponseDTO(any(DetalleOrden.class))).thenAnswer(inv -> {
            DetalleOrden d = inv.getArgument(0);
            return DetalleOrdenResponseDTO.builder()
                    .id(d.getId())
                    .productoId(d.getProductoId())
                    .cantidad(d.getCantidad())
                    .precioUnitario(d.getPrecioUnitario())
                    .subtotal(d.getSubtotal())
                    .build();
        });

        AgregarProductoRequestDTO request = AgregarProductoRequestDTO.builder()
                .productoId(productoId)
                .cantidad(cantidad)
                .build();

        DetalleOrdenResponseDTO response = ordenService.agregarProducto(ordenId, request);

        assertEquals(new BigDecimal("25000"), response.getPrecioUnitario());
        assertEquals(new BigDecimal("50000"), response.getSubtotal());

        DetalleOrden detalleGuardado = detalleCaptor.getValue();
        assertEquals(productoId, detalleGuardado.getProductoId());
        assertEquals(cantidad, detalleGuardado.getCantidad());
        assertEquals(new BigDecimal("25000"), detalleGuardado.getPrecioUnitario());
        assertEquals(new BigDecimal("50000"), detalleGuardado.getSubtotal());

        verify(productoClient, times(1)).obtenerProductoPorId(productoId);
    }
}

