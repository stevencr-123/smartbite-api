package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.ProductoIngredienteRequestDTO;
import com.smartbite.administrativo.dto.ProductoIngredienteResponseDTO;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.mapper.ProductoIngredienteMapper;
import com.smartbite.administrativo.model.ItemInventario;
import com.smartbite.administrativo.model.Producto;
import com.smartbite.administrativo.model.ProductoIngrediente;
import com.smartbite.administrativo.repository.ItemInventarioRepository;
import com.smartbite.administrativo.repository.ProductoIngredienteRepository;
import com.smartbite.administrativo.repository.ProductoRepository;
import com.smartbite.administrativo.service.ProductoIngredienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductoIngredienteServiceImpl implements ProductoIngredienteService {

    private final ProductoIngredienteRepository productoIngredienteRepository;
    private final ProductoRepository productoRepository;
    private final ItemInventarioRepository itemInventarioRepository;
    private final ProductoIngredienteMapper productoIngredienteMapper;

    @Override
    public ProductoIngredienteResponseDTO crearRelacion(ProductoIngredienteRequestDTO requestDTO) {
        log.info("Creando relación producto-ingrediente: productoId={}, itemInventarioId={}",
                requestDTO.getProductoId(), requestDTO.getItemInventarioId());

        // Validar que el producto existe y está activo
        Producto producto = productoRepository.findById(requestDTO.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + requestDTO.getProductoId()));

        if (!producto.getActivo()) {
            throw new BusinessException("No se puede agregar ingredientes a un producto inactivo");
        }

        // Validar que el ítem de inventario existe y está activo
        ItemInventario itemInventario = itemInventarioRepository.findById(requestDTO.getItemInventarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Ítem de inventario no encontrado con ID: " + requestDTO.getItemInventarioId()));

        if (!itemInventario.getActivo()) {
            throw new BusinessException("No se puede usar un ítem de inventario inactivo como ingrediente");
        }

        // Validar que no exista ya la relación
        if (productoIngredienteRepository.existsByProductoIdAndItemInventarioId(
                requestDTO.getProductoId(), requestDTO.getItemInventarioId())) {
            throw new BusinessException("Ya existe una relación entre este producto y este ingrediente");
        }

        // Validar cantidad
        if (requestDTO.getCantidadRequerida() <= 0) {
            throw new BusinessException("La cantidad requerida debe ser mayor a 0");
        }

        // Convertir DTO a Entity
        ProductoIngrediente relacion = productoIngredienteMapper.toEntity(requestDTO);

        // Asignar relaciones manualmente
        relacion.setProducto(producto);
        relacion.setItemInventario(itemInventario);
        relacion.setCantidadRequerida(requestDTO.getCantidadRequerida());
        relacion.setUnidadMedida(requestDTO.getUnidadMedida());

        ProductoIngrediente guardado = productoIngredienteRepository.save(relacion);
        log.info("Relación creada con ID: {}", guardado.getId());

        return productoIngredienteMapper.toResponseDTO(guardado);
    }

    @Override
    public ProductoIngredienteResponseDTO actualizarRelacion(Long id, ProductoIngredienteRequestDTO requestDTO) {
        log.info("Actualizando relación producto-ingrediente con ID: {}", id);

        ProductoIngrediente relacion = productoIngredienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relación no encontrada con ID: " + id));

        // Validar cantidad
        if (requestDTO.getCantidadRequerida() <= 0) {
            throw new BusinessException("La cantidad requerida debe ser mayor a 0");
        }

        // Actualizar campos
        relacion.setCantidadRequerida(requestDTO.getCantidadRequerida());
        relacion.setUnidadMedida(requestDTO.getUnidadMedida());

        // Si cambia el producto
        if (!relacion.getProducto().getId().equals(requestDTO.getProductoId())) {
            Producto nuevoProducto = productoRepository.findById(requestDTO.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + requestDTO.getProductoId()));

            if (!nuevoProducto.getActivo()) {
                throw new BusinessException("No se puede asignar un producto inactivo");
            }
            relacion.setProducto(nuevoProducto);
        }

        // Si cambia el ítem de inventario
        if (!relacion.getItemInventario().getId().equals(requestDTO.getItemInventarioId())) {
            ItemInventario nuevoItem = itemInventarioRepository.findById(requestDTO.getItemInventarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ítem de inventario no encontrado con ID: " + requestDTO.getItemInventarioId()));

            if (!nuevoItem.getActivo()) {
                throw new BusinessException("No se puede asignar un ítem de inventario inactivo");
            }
            relacion.setItemInventario(nuevoItem);
        }

        ProductoIngrediente actualizado = productoIngredienteRepository.save(relacion);
        log.info("Relación actualizada con ID: {}", actualizado.getId());

        return productoIngredienteMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoIngredienteResponseDTO obtenerRelacionPorId(Long id) {
        log.debug("Buscando relación con ID: {}", id);

        ProductoIngrediente relacion = productoIngredienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relación no encontrada con ID: " + id));

        return productoIngredienteMapper.toResponseDTO(relacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoIngredienteResponseDTO> obtenerRelacionesPorProducto(Long productoId) {
        log.debug("Obteniendo ingredientes del producto ID: {}", productoId);

        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + productoId);
        }

        return productoIngredienteRepository.findByProductoId(productoId).stream()
                .map(productoIngredienteMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoIngredienteResponseDTO> obtenerRelacionesPorItemInventario(Long itemInventarioId) {
        log.debug("Obteniendo productos que usan el ítem de inventario ID: {}", itemInventarioId);

        if (!itemInventarioRepository.existsById(itemInventarioId)) {
            throw new ResourceNotFoundException("Ítem de inventario no encontrado con ID: " + itemInventarioId);
        }

        return productoIngredienteRepository.findByItemInventarioId(itemInventarioId).stream()
                .map(productoIngredienteMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarRelacion(Long id) {
        log.info("Eliminando (desactivando) relación con ID: {}", id);

        ProductoIngrediente relacion = productoIngredienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relación no encontrada con ID: " + id));

        relacion.setActivo(false);
        productoIngredienteRepository.save(relacion);
        log.info("Relación desactivada con ID: {}", id);
    }

    @Override
    public ProductoIngredienteResponseDTO activarDesactivarRelacion(Long id, Boolean activo) {
        log.info("Cambiando estado de relación {} a activo={}", id, activo);

        ProductoIngrediente relacion = productoIngredienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relación no encontrada con ID: " + id));

        relacion.setActivo(activo);
        ProductoIngrediente actualizado = productoIngredienteRepository.save(relacion);

        return productoIngredienteMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularCostoProducto(Long productoId) {
        log.debug("Calculando costo del producto ID: {}", productoId);

        List<ProductoIngrediente> ingredientes = productoIngredienteRepository.findByProductoIdAndActivoTrue(productoId);

        if (ingredientes.isEmpty()) {
            log.warn("El producto ID: {} no tiene ingredientes definidos", productoId);
            return 0.0;
        }

        double costoTotal = 0.0;
        for (ProductoIngrediente ing : ingredientes) {
            ItemInventario item = ing.getItemInventario();
            Double costoItem = item.getCostoUnitario() != null ? item.getCostoUnitario() : 0.0;
            costoTotal += ing.getCantidadRequerida() * costoItem;
        }

        log.info("Costo calculado para producto ID {}: {}", productoId, costoTotal);
        return Math.round(costoTotal * 100.0) / 100.0;  // Redondear a 2 decimales
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validarStockSuficiente(Long productoId, Integer cantidad) {
        log.debug("Validando stock para producto ID: {}, cantidad: {}", productoId, cantidad);

        List<ProductoIngrediente> ingredientes = productoIngredienteRepository.findByProductoIdAndActivoTrue(productoId);

        if (ingredientes.isEmpty()) {
            log.warn("El producto ID: {} no tiene ingredientes definidos, validación omite stock", productoId);
            return true;  // Si no tiene ingredientes definidos, no validamos stock
        }

        for (ProductoIngrediente ing : ingredientes) {
            ItemInventario item = ing.getItemInventario();
            Double cantidadNecesaria = ing.getCantidadRequerida() * cantidad;

            // Convertir a la unidad del inventario (simplificado: asumimos misma unidad)
            if (item.getStockActual() < cantidadNecesaria) {
                log.warn("Stock insuficiente para {}: necesita {}, tiene {}",
                        item.getNombre(), cantidadNecesaria, item.getStockActual());
                return false;
            }
        }

        return true;
    }
}