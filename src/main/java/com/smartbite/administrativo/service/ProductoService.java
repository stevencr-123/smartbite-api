package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.ProductoRequestDTO;
import com.smartbite.administrativo.dto.ProductoResponseDTO;
import java.util.List;

public interface ProductoService {

    ProductoResponseDTO crearProducto(ProductoRequestDTO requestDTO);

    ProductoResponseDTO actualizarProducto(Long id, ProductoRequestDTO requestDTO);

    ProductoResponseDTO obtenerProductoPorId(Long id);

    List<ProductoResponseDTO> obtenerTodosLosProductos();

    List<ProductoResponseDTO> obtenerProductosPorCategoria(Long categoriaId);

    List<ProductoResponseDTO> obtenerProductosPorSucursal(Long sucursalId);

    List<ProductoResponseDTO> obtenerProductosActivos();

    List<ProductoResponseDTO> obtenerProductosDisponibles();

    List<ProductoResponseDTO> obtenerProductosDisponiblesPorSucursal(Long sucursalId);

    void eliminarProducto(Long id);  // Eliminación lógica

    ProductoResponseDTO activarDesactivarProducto(Long id, Boolean activo);

    ProductoResponseDTO cambiarDisponibilidad(Long id, Boolean disponible);

    Double obtenerPrecioProducto(Long id);  // CRÍTICO para el módulo Operativo

    boolean existsById(Long id);
}