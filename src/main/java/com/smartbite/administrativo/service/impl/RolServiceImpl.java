package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.RolRequestDTO;
import com.smartbite.administrativo.dto.RolResponseDTO;
import com.smartbite.administrativo.enums.RolNombre;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.mapper.RolMapper;
import com.smartbite.administrativo.model.Permiso;
import com.smartbite.administrativo.model.Rol;
import com.smartbite.administrativo.repository.PermisoRepository;
import com.smartbite.administrativo.repository.RolRepository;
import com.smartbite.administrativo.service.RolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolMapper rolMapper;

    @Override
    public RolResponseDTO crearRol(RolRequestDTO requestDTO) {
        log.info("Creando nuevo rol: {}", requestDTO.getNombre());

        if (rolRepository.existsByNombre(requestDTO.getNombre())) {
            throw new BusinessException("Ya existe un rol con el nombre: " + requestDTO.getNombre());
        }

        Rol rol = rolMapper.toEntity(requestDTO);
        Rol guardado = rolRepository.save(rol);
        log.info("Rol creado exitosamente con ID: {}", guardado.getId());

        return rolMapper.toResponseDTO(guardado);
    }

    @Override
    public RolResponseDTO actualizarRol(Long id, RolRequestDTO requestDTO) {
        log.info("Actualizando rol con ID: {}", id);

        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));

        if (rolRepository.existsByNombreAndIdNot(requestDTO.getNombre(), id)) {
            throw new BusinessException("Ya existe otro rol con el nombre: " + requestDTO.getNombre());
        }

        rolMapper.updateEntityFromRequest(requestDTO, rol);
        Rol actualizado = rolRepository.save(rol);
        log.info("Rol actualizado exitosamente");

        return rolMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public RolResponseDTO obtenerRolPorId(Long id) {
        log.debug("Buscando rol con ID: {}", id);
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));
        return rolMapper.toResponseDTO(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolResponseDTO> obtenerTodosLosRoles() {
        log.debug("Obteniendo todos los roles");
        return rolRepository.findAll().stream()
                .map(rolMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarRol(Long id) {
        log.info("Eliminando rol con ID: {}", id);
        if (!rolRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rol no encontrado con ID: " + id);
        }
        rolRepository.deleteById(id);
        log.info("Rol eliminado exitosamente");
    }

    @Override
    public RolResponseDTO activarDesactivarRol(Long id, Boolean activo) {
        log.info("Cambiando estado del rol {} a activo={}", id, activo);
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));
        rol.setActivo(activo);
        return rolMapper.toResponseDTO(rolRepository.save(rol));
    }

    @Override
    public RolResponseDTO asignarPermisos(Long rolId, List<Long> permisosIds) {
        log.info("Asignando permisos al rol ID: {}", rolId);
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + rolId));
        asignarPermisosARol(rol, permisosIds);
        return rolMapper.toResponseDTO(rolRepository.save(rol));
    }

    @Override
    public RolResponseDTO quitarPermiso(Long rolId, Long permisoId) {
        log.info("Quitando permiso {} del rol {}", permisoId, rolId);
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + rolId));
        rol.getPermisos().removeIf(p -> p.getId().equals(permisoId));
        return rolMapper.toResponseDTO(rolRepository.save(rol));
    }

    private void asignarPermisosARol(Rol rol, List<Long> permisosIds) {
        List<Permiso> permisos = permisoRepository.findAllById(permisosIds);
        if (permisos.size() != permisosIds.size()) {
            throw new BusinessException("Algunos permisos no existen");
        }
        rol.setPermisos(new HashSet<>(permisos));
    }
}