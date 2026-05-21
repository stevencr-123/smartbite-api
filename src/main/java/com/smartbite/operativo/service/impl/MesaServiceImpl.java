package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.client.SucursalClient;
import com.smartbite.operativo.dto.mesa.ActualizarEstadoMesaRequestDTO;
import com.smartbite.operativo.dto.mesa.CrearMesaRequestDTO;
import com.smartbite.operativo.dto.mesa.MesaResponseDTO;
import com.smartbite.operativo.exception.ResourceNotFoundException;
import com.smartbite.operativo.mapper.MesaMapper;
import com.smartbite.operativo.model.Mesa;
import com.smartbite.operativo.model.enums.EstadoMesa;
import com.smartbite.operativo.repository.MesaRepository;
import com.smartbite.operativo.service.MesaService;
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
    private final SucursalClient sucursalClient;

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
    public List<MesaResponseDTO> obtenerPorSucursalYEstado(
            Long sucursalId,
            EstadoMesa estado
    ) {

        return mesaRepository
                .findBySucursalIdAndEstado(sucursalId, estado)
                .stream()
                .map(mesaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MesaResponseDTO> obtenerDisponibles(Long sucursalId) {

        return mesaRepository
                .findBySucursalIdAndEstadoAndActivaTrue(
                        sucursalId,
                        EstadoMesa.DISPONIBLE
                )
                .stream()
                .map(mesaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MesaResponseDTO obtenerPorId(Long mesaId) {

        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mesa no encontrada con id: " + mesaId
                        ));

        return mesaMapper.toResponseDTO(mesa);
    }

    @Override
    @Transactional
    public MesaResponseDTO crearMesa(CrearMesaRequestDTO request) {

        sucursalClient.obtenerSucursalPorId(request.getSucursalId());

        Mesa mesa = Mesa.builder()
                .numero(request.getNumero())
                .capacidad(request.getCapacidad())
                .estado(EstadoMesa.DISPONIBLE)
                .activa(true)
                .sucursalId(request.getSucursalId())
                .build();

        Mesa mesaGuardada = mesaRepository.save(mesa);

        return mesaMapper.toResponseDTO(mesaGuardada);
    }

    @Override
    @Transactional
    public MesaResponseDTO actualizarMesa(
            Long id,
            CrearMesaRequestDTO request
    ) {

        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mesa no encontrada con id: " + id
                        ));

        sucursalClient.obtenerSucursalPorId(request.getSucursalId());

        mesa.setNumero(request.getNumero());
        mesa.setCapacidad(request.getCapacidad());
        mesa.setSucursalId(request.getSucursalId());

        Mesa mesaActualizada = mesaRepository.save(mesa);

        return mesaMapper.toResponseDTO(mesaActualizada);
    }

    @Override
    @Transactional
    public MesaResponseDTO actualizarEstado(
            Long mesaId,
            ActualizarEstadoMesaRequestDTO request
    ) {

        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mesa no encontrada con id: " + mesaId
                        ));

        mesa.setEstado(request.getEstado());

        Mesa mesaActualizada = mesaRepository.save(mesa);

        return mesaMapper.toResponseDTO(mesaActualizada);
    }

    @Override
    @Transactional
    public MesaResponseDTO cambiarEstadoActivo(
            Long mesaId,
            Boolean activa
    ) {

        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mesa no encontrada con id: " + mesaId
                        ));

        mesa.setActiva(activa);

        Mesa mesaActualizada = mesaRepository.save(mesa);

        return mesaMapper.toResponseDTO(mesaActualizada);
    }

    @Override
    @Transactional
    public void eliminarMesa(Long id) {

        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mesa no encontrada con id: " + id
                        ));

        mesaRepository.delete(mesa);
    }
}