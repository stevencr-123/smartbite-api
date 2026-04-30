package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.PermisoRequestDTO;
import com.smartbite.administrativo.dto.PermisoResponseDTO;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.mapper.PermisoMapper;
import com.smartbite.administrativo.model.Permiso;
import com.smartbite.administrativo.repository.PermisoRepository;
import com.smartbite.administrativo.service.PermisoService;
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
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository permisoRepository;
    private final PermisoMapper permisoMapper;

    @Override
    public PermisoResponseDTO crearPermiso(PermisoRequestDTO requestDTO) {
        log.info("Creando nuevo permiso: {}", requestDTO.getNombre());

        // Validar nombre único
        if (permisoRepository.existsByNombre(requestDTO.getNombre())) {
            throw new BusinessException("Ya existe un permiso con el nombre: " + requestDTO.getNombre());
        }

        Permiso permiso = permisoMapper.toEntity(requestDTO);
        Permiso guardado = permisoRepository.save(permiso);

        log.info("Permiso creado exitosamente con ID: {}", guardado.getId());
        return permisoMapper.toResponseDTO(guardado);
    }

    @Override
    public PermisoResponseDTO actualizarPermiso(Long id, PermisoRequestDTO requestDTO) {
        log.info("Actualizando permiso con ID: {}", id);

        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + id));

        // Validar nombre único excluyendo el propio permiso
        if (permisoRepository.existsByNombreAndIdNot(requestDTO.getNombre(), id)) {
            throw new BusinessException("Ya existe otro permiso con el nombre: " + requestDTO.getNombre());
        }

        permisoMapper.updateEntityFromRequest(requestDTO, permiso);
        Permiso actualizado = permisoRepository.save(permiso);

        log.info("Permiso actualizado exitosamente");
        return permisoMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public PermisoResponseDTO obtenerPermisoPorId(Long id) {
        log.debug("Buscando permiso con ID: {}", id);

        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + id));

        return permisoMapper.toResponseDTO(permiso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermisoResponseDTO> obtenerTodosLosPermisos() {
        log.debug("Obteniendo todos los permisos");

        return permisoRepository.findAll().stream()
                .map(permisoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarPermiso(Long id) {
        log.info("Eliminando permiso con ID: {}", id);

        if (!permisoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permiso no encontrado con ID: " + id);
        }

        permisoRepository.deleteById(id);
        log.info("Permiso eliminado exitosamente");
    }

    @Override
    public PermisoResponseDTO activarDesactivarPermiso(Long id, Boolean activo) {
        log.info("Cambiando estado del permiso {} a activo={}", id, activo);

        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + id));

        permiso.setActivo(activo);
        Permiso actualizado = permisoRepository.save(permiso);

        return permisoMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermisoResponseDTO> obtenerPermisosPorRecurso(String recurso) {
        log.debug("Obteniendo permisos por recurso: {}", recurso);

        return permisoRepository.findByRecurso(recurso).stream()
                .map(permisoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermisoResponseDTO> obtenerPermisosPorAccion(String accion) {
        log.debug("Obteniendo permisos por acción: {}", accion);

        return permisoRepository.findByAccion(accion).stream()
                .map(permisoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}