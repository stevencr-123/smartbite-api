package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.CompraRequestDTO;
import com.smartbite.administrativo.dto.CompraResponseDTO;
import com.smartbite.administrativo.dto.ReciboDetalleDTO;
import com.smartbite.administrativo.enums.EstadoCompra;
import java.time.LocalDateTime;
import java.util.List;

public interface CompraService {

    CompraResponseDTO crearCompra(CompraRequestDTO requestDTO);

    CompraResponseDTO actualizarCompra(Long id, CompraRequestDTO requestDTO);

    CompraResponseDTO obtenerCompraPorId(Long id);

    List<CompraResponseDTO> obtenerTodasLasCompras();

    List<CompraResponseDTO> obtenerComprasPorProvedor(Long provedorId);

    List<CompraResponseDTO> obtenerComprasPorSucursal(Long sucursalId);

    List<CompraResponseDTO> obtenerComprasPorEstado(EstadoCompra estado);

    List<CompraResponseDTO> obtenerComprasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin);

    CompraResponseDTO cambiarEstado(Long id, EstadoCompra estado);

    CompraResponseDTO recibirCompra(Long id, List<ReciboDetalleDTO> recibos);

    void anularCompra(Long id);

    void eliminarCompra(Long id);
}