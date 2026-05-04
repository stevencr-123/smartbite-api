package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.ItemInventarioRequestDTO;
import com.smartbite.administrativo.dto.ItemInventarioResponseDTO;
import java.util.List;

public interface ItemInventarioService {

    ItemInventarioResponseDTO crearItem(ItemInventarioRequestDTO requestDTO);

    ItemInventarioResponseDTO actualizarItem(Long id, ItemInventarioRequestDTO requestDTO);

    ItemInventarioResponseDTO obtenerItemPorId(Long id);

    List<ItemInventarioResponseDTO> obtenerTodosLosItems();

    List<ItemInventarioResponseDTO> obtenerItemsPorSucursal(Long sucursalId);

    List<ItemInventarioResponseDTO> obtenerItemsConStockBajo();

    List<ItemInventarioResponseDTO> obtenerItemsActivos();

    void eliminarItem(Long id);  // Eliminación lógica

    ItemInventarioResponseDTO activarDesactivarItem(Long id, Boolean activo);

    ItemInventarioResponseDTO ajustarStock(Long id, Integer cantidad, String motivo);

    boolean existsById(Long id);
}