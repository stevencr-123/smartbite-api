package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.dto.qr.CodigoQRResponseDTO;
import com.smartbite.operativo.dto.qr.GenerarQRRequestDTO;
import com.smartbite.operativo.exception.BusinessException;
import com.smartbite.operativo.exception.ResourceNotFoundException;
import com.smartbite.operativo.mapper.CodigoQRMapper;
import com.smartbite.operativo.model.CodigoQR;
import com.smartbite.operativo.model.Mesa;
import com.smartbite.operativo.model.enums.TipoQR;
import com.smartbite.operativo.repository.CodigoQRRepository;
import com.smartbite.operativo.repository.MesaRepository;
import com.smartbite.operativo.repository.OrdenRepository;
import com.smartbite.operativo.service.CodigoQRService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CodigoQRServiceImpl implements CodigoQRService {

    private final CodigoQRRepository codigoQRRepository;
    private final MesaRepository mesaRepository;
    private final OrdenRepository ordenRepository;
    private final CodigoQRMapper codigoQRMapper;

    @Override
    @Transactional
    public CodigoQRResponseDTO generarQR(GenerarQRRequestDTO request, Long ordenId) {
        // ordenId debe ser enviado externamente (ej: path param en controller futuro)
        validarConsistencia(request, ordenId);

        CodigoQR codigoQR = codigoQRMapper.toEntity(request);
        codigoQR.setContenido(generarContenido(request.getTipo()));
        codigoQR.setFechaGeneracion(LocalDateTime.now());
        codigoQR.setActivo(Boolean.TRUE);
        codigoQR.setOrdenId(ordenId);

        if (request.getTipo() == TipoQR.MESA) {
            Mesa mesa = mesaRepository.findById(request.getMesaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada con id: " + request.getMesaId()));
            codigoQR.setMesa(mesa);
        }

        if (request.getTipo() == TipoQR.PRODUCTO) {
            codigoQR.setProductoId(request.getProductoId());
        }

        if (request.getTipo() == TipoQR.PAGO) {
            if (!ordenRepository.existsById(ordenId)) {
                throw new ResourceNotFoundException("Orden no encontrada con id: " + ordenId);
            }
            codigoQR.setOrdenId(ordenId);
        }

        CodigoQR codigoQRGuardado = codigoQRRepository.save(codigoQR);
        return codigoQRMapper.toResponseDTO(codigoQRGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public CodigoQRResponseDTO obtenerPorId(Long codigoQrId) {
        CodigoQR codigoQR = codigoQRRepository.findById(codigoQrId)
                .orElseThrow(() -> new ResourceNotFoundException("CodigoQR no encontrado con id: " + codigoQrId));
        return codigoQRMapper.toResponseDTO(codigoQR);
    }

    private void validarConsistencia(GenerarQRRequestDTO request, Long ordenId) {

        if (request == null || request.getTipo() == null) {
            throw new BusinessException("El tipo de QR es obligatorio");
        }

        TipoQR tipo = request.getTipo();

        switch (tipo) {

            case MESA -> {
                if (request.getMesaId() == null) {
                    throw new BusinessException("mesaId es obligatorio para QR tipo MESA");
                }
                if (request.getProductoId() != null || ordenId != null) {
                    throw new BusinessException("QR tipo MESA solo debe tener mesaId");
                }
            }

            case PRODUCTO -> {
                if (request.getProductoId() == null) {
                    throw new BusinessException("productoId es obligatorio para QR tipo PRODUCTO");
                }
                if (request.getMesaId() != null || ordenId != null) {
                    throw new BusinessException("QR tipo PRODUCTO solo debe tener productoId");
                }
            }

            case PAGO -> {
                if (ordenId == null) {
                    throw new BusinessException("ordenId es obligatorio para QR tipo PAGO");
                }
                if (request.getMesaId() != null || request.getProductoId() != null) {
                    throw new BusinessException("QR tipo PAGO solo debe tener ordenId");
                }
            }
        }
    }

    private String generarContenido(TipoQR tipo) {
        return "SB-" + tipo.name() + "-" + UUID.randomUUID();
    }
}

