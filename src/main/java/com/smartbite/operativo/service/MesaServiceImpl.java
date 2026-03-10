package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.mesa.ActualizarEstadoMesaRequestDTO;
import com.smartbite.operativo.dto.mesa.MesaResponseDTO;
import com.smartbite.operativo.exception.ResourceNotFoundException;
import com.smartbite.operativo.mapper.MesaMapper;
import com.smartbite.operativo.model.Mesa;
import com.smartbite.operativo.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MesaServiceImpl implements MesaService {

    private final MesaRepository mesaRepository;
    private final MesaMapper mesaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MesaResponseDTO> obtenerTodas() {
        return mesaRepository.findAll()
                .stream()
                .map(mesaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MesaResponseDTO> obtenerPorSucursal(Long sucursalId) {
        return mesaRepository.findBySucursalId(sucursalId)
                .stream()
                .map(mesaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MesaResponseDTO obtenerPorId(Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada con id: " + mesaId));
        return mesaMapper.toResponseDTO(mesa);
    }

    @Override
    @Transactional
    public MesaResponseDTO actualizarEstado(Long mesaId, ActualizarEstadoMesaRequestDTO request) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada con id: " + mesaId));

        mesa.setEstado(request.getEstado());
        Mesa mesaActualizada = mesaRepository.save(mesa);
        return mesaMapper.toResponseDTO(mesaActualizada);
    }
}
