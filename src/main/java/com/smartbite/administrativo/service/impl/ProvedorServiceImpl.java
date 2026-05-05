package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.ProvedorRequestDTO;
import com.smartbite.administrativo.dto.ProvedorResponseDTO;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.mapper.ProvedorMapper;
import com.smartbite.administrativo.model.Provedor;
import com.smartbite.administrativo.repository.ProvedorRepository;
import com.smartbite.administrativo.service.ProvedorService;
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
public class ProvedorServiceImpl implements ProvedorService {

    private final ProvedorRepository provedorRepository;
    private final ProvedorMapper provedorMapper;

    @Override
    public ProvedorResponseDTO crearProvedor(ProvedorRequestDTO requestDTO) {
        log.info("Creando provedor: {}", requestDTO.getNombre());

        if (requestDTO.getNit() != null && provedorRepository.existsByNit(requestDTO.getNit())) {
            throw new BusinessException("Ya existe un provedor con el NIT: " + requestDTO.getNit());
        }

        if (requestDTO.getEmail() != null && provedorRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new BusinessException("Ya existe un provedor con el email: " + requestDTO.getEmail());
        }

        Provedor provedor = provedorMapper.toEntity(requestDTO);
        Provedor guardado = provedorRepository.save(provedor);
        log.info("Provedor creado con ID: {}", guardado.getId());

        return provedorMapper.toResponseDTO(guardado);
    }

    @Override
    public ProvedorResponseDTO actualizarProvedor(Long id, ProvedorRequestDTO requestDTO) {
        log.info("Actualizando provedor con ID: {}", id);

        Provedor provedor = provedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provedor no encontrado con ID: " + id));

        if (requestDTO.getNit() != null && provedorRepository.existsByNitAndIdNot(requestDTO.getNit(), id)) {
            throw new BusinessException("Ya existe otro provedor con el NIT: " + requestDTO.getNit());
        }

        if (requestDTO.getEmail() != null && provedorRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
            throw new BusinessException("Ya existe otro provedor con el email: " + requestDTO.getEmail());
        }

        provedorMapper.updateEntityFromRequest(requestDTO, provedor);
        Provedor actualizado = provedorRepository.save(provedor);
        log.info("Provedor actualizado con ID: {}", actualizado.getId());

        return provedorMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProvedorResponseDTO obtenerProvedorPorId(Long id) {
        log.debug("Buscando provedor con ID: {}", id);

        Provedor provedor = provedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provedor no encontrado con ID: " + id));

        return provedorMapper.toResponseDTO(provedor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvedorResponseDTO> obtenerTodosLosProvedores() {
        log.debug("Obteniendo todos los provedores");

        return provedorRepository.findAll().stream()
                .map(provedorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvedorResponseDTO> obtenerProvedoresActivos() {
        log.debug("Obteniendo provedores activos");

        return provedorRepository.findByActivoTrue().stream()
                .map(provedorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvedorResponseDTO> buscarProvedoresPorNombre(String nombre) {
        log.debug("Buscando provedores por nombre: {}", nombre);

        return provedorRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(provedorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarProvedor(Long id) {
        log.info("Eliminando (desactivando) provedor con ID: {}", id);

        Provedor provedor = provedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provedor no encontrado con ID: " + id));

        provedor.setActivo(false);
        provedorRepository.save(provedor);
        log.info("Provedor desactivado con ID: {}", id);
    }

    @Override
    public ProvedorResponseDTO activarDesactivarProvedor(Long id, Boolean activo) {
        log.info("Cambiando estado del provedor {} a activo={}", id, activo);

        Provedor provedor = provedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provedor no encontrado con ID: " + id));

        provedor.setActivo(activo);
        Provedor actualizado = provedorRepository.save(provedor);

        return provedorMapper.toResponseDTO(actualizado);
    }
}