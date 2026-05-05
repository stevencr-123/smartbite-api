package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.*;
import com.smartbite.administrativo.enums.EstadoCompra;
import com.smartbite.administrativo.enums.TipoMovimientoInventario;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.mapper.CompraMapper;
import com.smartbite.administrativo.mapper.DetalleCompraMapper;
import com.smartbite.administrativo.model.*;
import com.smartbite.administrativo.repository.*;
import com.smartbite.administrativo.service.CompraService;
import com.smartbite.administrativo.service.MovimientoInventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProvedorRepository provedorRepository;
    private final SucursalRepository sucursalRepository;
    private final ItemInventarioRepository itemInventarioRepository;
    private final MovimientoInventarioService movimientoInventarioService;
    private final CompraMapper compraMapper;
    private final DetalleCompraMapper detalleCompraMapper;

    @Override
    public CompraResponseDTO crearCompra(CompraRequestDTO requestDTO) {
        log.info("Creando compra para provedor ID: {}", requestDTO.getProvedorId());

        Provedor provedor = provedorRepository.findById(requestDTO.getProvedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Provedor no encontrado con ID: " + requestDTO.getProvedorId()));

        if (!provedor.getActivo()) {
            throw new BusinessException("No se puede crear compra para un provedor inactivo");
        }

        Sucursal sucursal = sucursalRepository.findById(requestDTO.getSucursalId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + requestDTO.getSucursalId()));

        if (!sucursal.getActivo()) {
            throw new BusinessException("No se puede crear compra para una sucursal inactiva");
        }

        Compra compra = new Compra();
        compra.setProvedor(provedor);
        compra.setSucursal(sucursal);
        compra.setFechaCompra(requestDTO.getFechaCompra() != null ? requestDTO.getFechaCompra() : LocalDateTime.now());
        compra.setNumeroFactura(requestDTO.getNumeroFactura());
        compra.setEstado(EstadoCompra.PENDIENTE);

        double total = 0.0;

        for (DetalleCompraRequestDTO detalleDTO : requestDTO.getDetalles()) {
            ItemInventario item = itemInventarioRepository.findById(detalleDTO.getItemInventarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ítem de inventario no encontrado con ID: " + detalleDTO.getItemInventarioId()));

            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compra);
            detalle.setItemInventario(item);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
            detalle.setSubtotal(detalleDTO.getCantidad() * detalleDTO.getPrecioUnitario());
            detalle.setCantidadRecibida(0);

            compra.getDetalles().add(detalle);
            total += detalle.getSubtotal();
        }

        compra.setTotal(total);
        Compra guardado = compraRepository.save(compra);
        log.info("Compra creada con ID: {}, Total: {}", guardado.getId(), guardado.getTotal());

        return compraMapper.toResponseDTO(guardado);
    }

    @Override
    public CompraResponseDTO actualizarCompra(Long id, CompraRequestDTO requestDTO) {
        log.info("Actualizando compra con ID: {}", id);

        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));

        if (compra.getEstado() != EstadoCompra.PENDIENTE) {
            throw new BusinessException("Solo se pueden modificar compras en estado PENDIENTE");
        }

        detalleCompraRepository.deleteAll(compra.getDetalles());
        compra.getDetalles().clear();

        double total = 0.0;

        for (DetalleCompraRequestDTO detalleDTO : requestDTO.getDetalles()) {
            ItemInventario item = itemInventarioRepository.findById(detalleDTO.getItemInventarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ítem de inventario no encontrado"));

            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compra);
            detalle.setItemInventario(item);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
            detalle.setSubtotal(detalleDTO.getCantidad() * detalleDTO.getPrecioUnitario());

            compra.getDetalles().add(detalle);
            total += detalle.getSubtotal();
        }

        compra.setTotal(total);
        Compra actualizado = compraRepository.save(compra);
        log.info("Compra actualizada con ID: {}", actualizado.getId());

        return compraMapper.toResponseDTO(actualizado);
    }

    @Override
    public CompraResponseDTO recibirCompra(Long id, List<ReciboDetalleDTO> recibos) {
        log.info("Recibiendo compra con ID: {}", id);

        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));

        if (compra.getEstado() == EstadoCompra.CANCELADA) {
            throw new BusinessException("No se puede recibir una compra cancelada");
        }

        boolean todosRecibidos = true;
        boolean algunRecibido = false;

        for (ReciboDetalleDTO recibo : recibos) {
            DetalleCompra detalle = detalleCompraRepository.findById(recibo.getDetalleCompraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado"));

            if (!detalle.getCompra().getId().equals(id)) {
                throw new BusinessException("El detalle no pertenece a esta compra");
            }

            if (detalle.getCantidadRecibida() < detalle.getCantidad()) {
                int nuevaCantidadRecibida = detalle.getCantidadRecibida() + recibo.getCantidadRecibida();

                if (nuevaCantidadRecibida > detalle.getCantidad()) {
                    throw new BusinessException("La cantidad recibida excede la cantidad solicitada");
                }

                detalle.setCantidadRecibida(nuevaCantidadRecibida);

                movimientoInventarioService.registrarEntrada(
                        detalle.getItemInventario().getId(),
                        recibo.getCantidadRecibida(),
                        "Compra ID: " + id + " - " + compra.getNumeroFactura(),
                        "COMPRA",
                        id
                );

                detalleCompraRepository.save(detalle);
                algunRecibido = true;
            }

            if (detalle.getCantidadRecibida() < detalle.getCantidad()) {
                todosRecibidos = false;
            }
        }

        if (todosRecibidos) {
            compra.setEstado(EstadoCompra.COMPLETADA);
        } else if (algunRecibido) {
            compra.setEstado(EstadoCompra.PARCIAL);
        }

        Compra actualizado = compraRepository.save(compra);
        log.info("Compra recibida. Nuevo estado: {}", actualizado.getEstado());

        return compraMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public CompraResponseDTO cambiarEstado(Long id, EstadoCompra estado) {
        log.info("Cambiando estado de compra {} a {}", id, estado);

        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));

        compra.setEstado(estado);
        Compra actualizado = compraRepository.save(compra);

        return compraMapper.toResponseDTO(actualizado);
    }

    @Override
    public void anularCompra(Long id) {
        log.info("Anulando compra con ID: {}", id);

        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));

        if (compra.getEstado() == EstadoCompra.COMPLETADA) {
            throw new BusinessException("No se puede anular una compra ya completada");
        }

        compra.setEstado(EstadoCompra.CANCELADA);
        compraRepository.save(compra);
        log.info("Compra anulada con ID: {}", id);
    }

    @Override
    public void eliminarCompra(Long id) {
        log.info("Eliminando compra con ID: {}", id);

        if (!compraRepository.existsById(id)) {
            throw new ResourceNotFoundException("Compra no encontrada con ID: " + id);
        }

        compraRepository.deleteById(id);
        log.info("Compra eliminada con ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CompraResponseDTO obtenerCompraPorId(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));
        return compraMapper.toResponseDTO(compra);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> obtenerTodasLasCompras() {
        return compraRepository.findAll().stream()
                .map(compraMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> obtenerComprasPorProvedor(Long provedorId) {
        return compraRepository.findByProvedorId(provedorId).stream()
                .map(compraMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> obtenerComprasPorSucursal(Long sucursalId) {
        return compraRepository.findBySucursalId(sucursalId).stream()
                .map(compraMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> obtenerComprasPorEstado(EstadoCompra estado) {
        return compraRepository.findByEstado(estado).stream()
                .map(compraMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> obtenerComprasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return compraRepository.findByFechaCompraBetween(inicio, fin).stream()
                .map(compraMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}