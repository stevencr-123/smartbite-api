package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.ProductoIngredienteRequestDTO;
import com.smartbite.administrativo.dto.ProductoIngredienteResponseDTO;
import java.util.List;

public interface ProductoIngredienteService {

    ProductoIngredienteResponseDTO crearRelacion(ProductoIngredienteRequestDTO requestDTO);

    ProductoIngredienteResponseDTO actualizarRelacion(Long id, ProductoIngredienteRequestDTO requestDTO);

    ProductoIngredienteResponseDTO obtenerRelacionPorId(Long id);

    List<ProductoIngredienteResponseDTO> obtenerRelacionesPorProducto(Long productoId);

    List<ProductoIngredienteResponseDTO> obtenerRelacionesPorItemInventario(Long itemInventarioId);

    void eliminarRelacion(Long id);

    ProductoIngredienteResponseDTO activarDesactivarRelacion(Long id, Boolean activo);

    Double calcularCostoProducto(Long productoId);  // Suma de (cantidad * costo de cada item)

    boolean validarStockSuficiente(Long productoId, Integer cantidad);  // Para el módulo Operativo
}