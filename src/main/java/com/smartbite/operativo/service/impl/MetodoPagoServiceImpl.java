package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.dto.metodopago.MetodoPagoResponseDTO;
import com.smartbite.operativo.exception.ResourceNotFoundException;
import com.smartbite.operativo.mapper.MetodoPagoMapper;
import com.smartbite.operativo.model.MetodoPago;
import com.smartbite.operativo.repository.MetodoPagoRepository;
import com.smartbite.operativo.service.MetodoPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetodoPagoServiceImpl implements MetodoPagoService {

    private final MetodoPagoRepository metodoPagoRepository;
    private final MetodoPagoMapper metodoPagoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MetodoPagoResponseDTO> obtenerTodos() {

        return metodoPagoRepository.findAll()
                .stream()
                .map(metodoPagoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetodoPagoResponseDTO> obtenerActivos() {

        return metodoPagoRepository.findAll()
                .stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .map(metodoPagoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MetodoPagoResponseDTO obtenerPorId(Long id) {

        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Método de pago no encontrado con id: " + id));

        return metodoPagoMapper.toResponseDTO(metodoPago);
    }
}