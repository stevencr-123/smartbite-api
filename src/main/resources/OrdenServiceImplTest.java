package com.smartbite.operativo.service;

import com.smartbite.App;
import com.smartbite.operativo.client.ProductoClient;
import com.smartbite.operativo.client.dto.ProductoDTO;
import com.smartbite.operativo.exception.BusinessException;
import com.smartbite.operativo.exception.InvalidStateException;
import com.smartbite.operativo.exception.ResourceNotFoundException;
import com.smartbite.operativo.mapper.DetalleOrdenMapper;
import com.smartbite.operativo.mapper.OrdenMapper;
import com.smartbite.operativo.repository.DetalleOrdenRepository;
import com.smartbite.operativo.repository.MesaRepository;
import com.smartbite.operativo.repository.OrdenRepository;
import com.smartbite.operativo.service.impl.OrdenServiceImpl;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = App.class)
@ActiveProfiles("test")
class OrdenServiceImplTest {

    @Autowired
    private OrdenServiceImpl ordenService;

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
    void productoValido_retornaPrecio() {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(1L);
        dto.setNombre("Producto");
        dto.setActivo(true);
        dto.setPrecio(new BigDecimal("25000"));

        when(productoClient.obtenerProductoPorId(1L)).thenReturn(dto);

        BigDecimal precio = ReflectionTestUtils.invokeMethod(ordenService, "obtenerPrecioProducto", 1L);

        assertEquals(new BigDecimal("25000"), precio);
    }

    @Test
    void productoNoExiste_lanzaResourceNotFoundException() {
        when(productoClient.obtenerProductoPorId(1L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () ->
                ReflectionTestUtils.invokeMethod(ordenService, "obtenerPrecioProducto", 1L));
    }

    @Test
    void productoInactivo_lanzaInvalidStateException() {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(1L);
        dto.setNombre("Producto");
        dto.setActivo(false);
        dto.setPrecio(new BigDecimal("25000"));

        when(productoClient.obtenerProductoPorId(1L)).thenReturn(dto);

        assertThrows(InvalidStateException.class, () ->
                ReflectionTestUtils.invokeMethod(ordenService, "obtenerPrecioProducto", 1L));
    }

    @Test
    void precioInvalido_lanzaInvalidStateException() {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(1L);
        dto.setNombre("Producto");
        dto.setActivo(true);
        dto.setPrecio(BigDecimal.ZERO);

        when(productoClient.obtenerProductoPorId(1L)).thenReturn(dto);

        assertThrows(InvalidStateException.class, () ->
                ReflectionTestUtils.invokeMethod(ordenService, "obtenerPrecioProducto", 1L));
    }

    @Test
    void feignNotFound_lanzaResourceNotFoundException() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/productos/1", Map.of(), null,
                StandardCharsets.UTF_8, null);
        FeignException notFound = new FeignException.NotFound("Not found", request, null, null);

        when(productoClient.obtenerProductoPorId(1L)).thenThrow(notFound);

        assertThrows(ResourceNotFoundException.class, () ->
                ReflectionTestUtils.invokeMethod(ordenService, "obtenerPrecioProducto", 1L));
    }

    @Test
    void feignError_lanzaBusinessException() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/productos/1", Map.of(), null,
                StandardCharsets.UTF_8, null);
        FeignException error = new FeignException.InternalServerError("Error", request, null, null);

        when(productoClient.obtenerProductoPorId(1L)).thenThrow(error);

        assertThrows(BusinessException.class, () ->
                ReflectionTestUtils.invokeMethod(ordenService, "obtenerPrecioProducto", 1L));
    }
}

