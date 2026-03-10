package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.detalle.AgregarProductoRequestDTO;
import com.smartbite.operativo.dto.detalle.DetalleOrdenResponseDTO;
import com.smartbite.operativo.dto.orden.CrearOrdenRequestDTO;
import com.smartbite.operativo.dto.orden.OrdenResumenDTO;
import com.smartbite.operativo.dto.orden.OrdenResponseDTO;

import java.util.List;

public interface OrdenService {

    OrdenResponseDTO crearOrden(CrearOrdenRequestDTO request);

    DetalleOrdenResponseDTO agregarProducto(Long ordenId, AgregarProductoRequestDTO request);

    OrdenResponseDTO obtenerOrdenPorId(Long ordenId);

    List<OrdenResumenDTO> obtenerOrdenesActivas();

    OrdenResponseDTO recalcularTotal(Long ordenId);

    OrdenResponseDTO cambiarEstado(Long ordenId, String nuevoEstado);

    OrdenResponseDTO cerrarOrden(Long ordenId);
}

