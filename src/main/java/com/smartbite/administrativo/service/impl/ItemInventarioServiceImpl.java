package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.ItemInventarioRequestDTO;
import com.smartbite.administrativo.dto.ItemInventarioResponseDTO;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.mapper.ItemInventarioMapper;
import com.smartbite.administrativo.model.ItemInventario;
import com.smartbite.administrativo.model.Sucursal;
import com.smartbite.administrativo.repository.ItemInventarioRepository;
import com.smartbite.administrativo.repository.SucursalRepository;
import com.smartbite.administrativo.service.ItemInventarioService;
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
public class ItemInventarioServiceImpl implements ItemInventarioService {

    private final ItemInventarioRepository itemInventarioRepository;
    private final SucursalRepository sucursalRepository;
    private final ItemInventarioMapper itemInventarioMapper;

    @Override
    public ItemInventarioResponseDTO crearItem(ItemInventarioRequestDTO requestDTO) {
        log.info("Creando ítem de inventario: {}", requestDTO.getNombre());

        // Validar nombre único
        if (itemInventarioRepository.existsByNombre(requestDTO.getNombre())) {
            throw new BusinessException("Ya existe un ítem con el nombre: " + requestDTO.getNombre());
        }

        // Validar stock
        if (requestDTO.getStockActual() < 0) {
            throw new BusinessException("El stock actual no puede ser negativo");
        }
        if (requestDTO.getStockMinimo() < 0) {
            throw new BusinessException("El stock mínimo no puede ser negativo");
        }

        // Validar que la sucursal existe y está activa
        Sucursal sucursal = sucursalRepository.findById(requestDTO.getSucursalId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + requestDTO.getSucursalId()));

        if (!sucursal.getActivo()) {
            throw new BusinessException("No se puede asignar un ítem a una sucursal inactiva");
        }

        // Convertir DTO a Entity
        ItemInventario item = itemInventarioMapper.toEntity(requestDTO);
        item.setSucursal(sucursal);

        ItemInventario guardado = itemInventarioRepository.save(item);
        log.info("Ítem de inventario creado con ID: {}, Stock inicial: {}", guardado.getId(), guardado.getStockActual());

        return itemInventarioMapper.toResponseDTO(guardado);
    }

    @Override
    public ItemInventarioResponseDTO actualizarItem(Long id, ItemInventarioRequestDTO requestDTO) {
        log.info("Actualizando ítem de inventario con ID: {}", id);

        ItemInventario item = itemInventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado con ID: " + id));

        // Validar nombre único excluyendo el propio item
        if (itemInventarioRepository.existsByNombreAndIdNot(requestDTO.getNombre(), id)) {
            throw new BusinessException("Ya existe otro ítem con el nombre: " + requestDTO.getNombre());
        }

        // Validar stock
        if (requestDTO.getStockActual() < 0) {
            throw new BusinessException("El stock actual no puede ser negativo");
        }
        if (requestDTO.getStockMinimo() < 0) {
            throw new BusinessException("El stock mínimo no puede ser negativo");
        }

        // Actualizar campos básicos
        item.setNombre(requestDTO.getNombre());
        item.setDescripcion(requestDTO.getDescripcion());
        item.setStockActual(requestDTO.getStockActual());
        item.setStockMinimo(requestDTO.getStockMinimo());
        item.setUnidadMedida(requestDTO.getUnidadMedida());
        item.setCostoUnitario(requestDTO.getCostoUnitario());
        item.setUbicacion(requestDTO.getUbicacion());

        // Actualizar sucursal si cambia
        if (!item.getSucursal().getId().equals(requestDTO.getSucursalId())) {
            Sucursal nuevaSucursal = sucursalRepository.findById(requestDTO.getSucursalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + requestDTO.getSucursalId()));

            if (!nuevaSucursal.getActivo()) {
                throw new BusinessException("No se puede asignar un ítem a una sucursal inactiva");
            }
            item.setSucursal(nuevaSucursal);
        }

        ItemInventario actualizado = itemInventarioRepository.save(item);
        log.info("Ítem actualizado con ID: {}", actualizado.getId());

        return itemInventarioMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemInventarioResponseDTO obtenerItemPorId(Long id) {
        log.debug("Buscando ítem con ID: {}", id);

        ItemInventario item = itemInventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado con ID: " + id));

        return itemInventarioMapper.toResponseDTO(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemInventarioResponseDTO> obtenerTodosLosItems() {
        log.debug("Obteniendo todos los ítems de inventario");

        return itemInventarioRepository.findAll().stream()
                .map(itemInventarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemInventarioResponseDTO> obtenerItemsPorSucursal(Long sucursalId) {
        log.debug("Obteniendo ítems de la sucursal ID: {}", sucursalId);

        if (!sucursalRepository.existsById(sucursalId)) {
            throw new ResourceNotFoundException("Sucursal no encontrada con ID: " + sucursalId);
        }

        return itemInventarioRepository.findBySucursalId(sucursalId).stream()
                .map(itemInventarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemInventarioResponseDTO> obtenerItemsConStockBajo() {
        log.debug("Obteniendo ítems con stock bajo");

        return itemInventarioRepository.findByStockActualLessThanEqual(0).stream()
                .map(itemInventarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemInventarioResponseDTO> obtenerItemsActivos() {
        log.debug("Obteniendo ítems activos");

        return itemInventarioRepository.findByActivoTrue().stream()
                .map(itemInventarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarItem(Long id) {
        log.info("Eliminando (desactivando) ítem con ID: {}", id);

        ItemInventario item = itemInventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado con ID: " + id));

        item.setActivo(false);
        itemInventarioRepository.save(item);
        log.info("Ítem desactivado con ID: {}", id);
    }

    @Override
    public ItemInventarioResponseDTO activarDesactivarItem(Long id, Boolean activo) {
        log.info("Cambiando estado del ítem {} a activo={}", id, activo);

        ItemInventario item = itemInventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado con ID: " + id));

        item.setActivo(activo);
        ItemInventario actualizado = itemInventarioRepository.save(item);

        return itemInventarioMapper.toResponseDTO(actualizado);
    }

    @Override
    public ItemInventarioResponseDTO ajustarStock(Long id, Integer cantidad, String motivo) {
        log.info("Ajustando stock del ítem {}: cantidad={}, motivo={}", id, cantidad, motivo);

        ItemInventario item = itemInventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ítem no encontrado con ID: " + id));

        Integer nuevoStock = item.getStockActual() + cantidad;

        if (nuevoStock < 0) {
            throw new BusinessException("El ajuste dejaría el stock negativo. Stock actual: " + item.getStockActual());
        }

        item.setStockActual(nuevoStock);
        ItemInventario actualizado = itemInventarioRepository.save(item);

        log.info("Stock ajustado. Nuevo stock: {}, Motivo: {}", nuevoStock, motivo);



        return itemInventarioMapper.toResponseDTO(actualizado);
    }

    @Override
    public boolean existsById(Long id) {
        return itemInventarioRepository.existsById(id);
    }
}