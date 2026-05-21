package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.UsuarioRequestDTO;
import com.smartbite.administrativo.dto.UsuarioResponseDTO;
import com.smartbite.administrativo.enums.RolNombre;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.mapper.UsuarioMapper;
import com.smartbite.administrativo.model.Rol;
import com.smartbite.administrativo.model.Sucursal;
import com.smartbite.administrativo.model.Usuario;
import com.smartbite.administrativo.repository.RolRepository;
import com.smartbite.administrativo.repository.SucursalRepository;
import com.smartbite.administrativo.repository.UsuarioRepository;
import com.smartbite.administrativo.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO requestDTO) {
        log.info("Creando usuario: {}", requestDTO.getEmail());

        // Validar email único
        if (usuarioRepository.existsByEmail(requestDTO.getEmail())) {
            throw new BusinessException("Ya existe un usuario con el email: " + requestDTO.getEmail());
        }

        // Validar que la sucursal existe y está activa
        Sucursal sucursal = sucursalRepository.findById(requestDTO.getSucursalId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + requestDTO.getSucursalId()));

        if (!sucursal.getActivo()) {
            throw new BusinessException("No se puede asignar un usuario a una sucursal inactiva");
        }

        // 🔁 CORREGIDO: Buscar rol por NOMBRE (enum), no por ID
        Rol rol = rolRepository.findByNombre(requestDTO.getRolNombre())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + requestDTO.getRolNombre()));

        if (!rol.getActivo()) {
            throw new BusinessException("No se puede asignar un rol inactivo al usuario");
        }

        // Validar longitud de contraseña
        if (requestDTO.getPassword().length() < 6) {
            throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
        }

        // Convertir DTO a Entity (sin relaciones ni password)
        Usuario usuario = usuarioMapper.toEntity(requestDTO);

        // Asignar relaciones manualmente
        usuario.setSucursal(sucursal);
        usuario.setRol(rol);

        // Encriptar password
        usuario.setPassword(passwordEncoder.encode(requestDTO.getPassword()));

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado con ID: {}", guardado.getId());

        return usuarioMapper.toResponseDTO(guardado);
    }

    @Override
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO requestDTO) {
        log.info("Actualizando usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        // Validar email único excluyendo el propio usuario
        if (usuarioRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
            throw new BusinessException("Ya existe otro usuario con el email: " + requestDTO.getEmail());
        }

        // Actualizar campos básicos
        usuario.setNombre(requestDTO.getNombre());
        usuario.setEmail(requestDTO.getEmail());
        usuario.setTelefono(requestDTO.getTelefono());

        // Solo actualizar contraseña si se proporcionó una nueva
        if (requestDTO.getPassword() != null && !requestDTO.getPassword().isEmpty()) {
            if (requestDTO.getPassword().length() < 6) {
                throw new BusinessException("La contraseña debe tener al menos 6 caracteres");
            }
            usuario.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        }

        // Actualizar sucursal si cambia
        if (!usuario.getSucursal().getId().equals(requestDTO.getSucursalId())) {
            Sucursal nuevaSucursal = sucursalRepository.findById(requestDTO.getSucursalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + requestDTO.getSucursalId()));

            if (!nuevaSucursal.getActivo()) {
                throw new BusinessException("No se puede asignar un usuario a una sucursal inactiva");
            }
            usuario.setSucursal(nuevaSucursal);
        }

        // 🔁 CORREGIDO: Actualizar rol si cambia (buscando por nombre)
        if (usuario.getRol().getNombre() != requestDTO.getRolNombre()) {
            Rol nuevoRol = rolRepository.findByNombre(requestDTO.getRolNombre())
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + requestDTO.getRolNombre()));

            if (!nuevoRol.getActivo()) {
                throw new BusinessException("No se puede asignar un rol inactivo al usuario");
            }
            usuario.setRol(nuevoRol);
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        log.info("Usuario actualizado con ID: {}", actualizado.getId());

        return usuarioMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerUsuarioPorId(Long id) {
        log.debug("Buscando usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        return usuarioMapper.toResponseDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodosLosUsuarios() {
        log.debug("Obteniendo todos los usuarios");

        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerUsuariosPorSucursal(Long sucursalId) {
        log.debug("Obteniendo usuarios de la sucursal ID: {}", sucursalId);

        if (!sucursalRepository.existsById(sucursalId)) {
            throw new ResourceNotFoundException("Sucursal no encontrada con ID: " + sucursalId);
        }

        return usuarioRepository.findBySucursalId(sucursalId).stream()
                .map(usuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // 🔁 CORREGIDO: obtenerUsuariosPorRol ahora recibe RolNombre
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerUsuariosPorRol(RolNombre rolNombre) {
        log.debug("Obteniendo usuarios con rol: {}", rolNombre);

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + rolNombre));

        return usuarioRepository.findByRolId(rol.getId()).stream()
                .map(usuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerUsuariosActivos() {
        log.debug("Obteniendo usuarios activos");

        return usuarioRepository.findByActivoTrue().stream()
                .map(usuarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarUsuario(Long id) {
        log.info("Eliminando (desactivando) usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        log.info("Usuario desactivado con ID: {}", id);
    }

    @Override
    public UsuarioResponseDTO activarDesactivarUsuario(Long id, Boolean activo) {
        log.info("Cambiando estado del usuario {} a activo={}", id, activo);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(activo);
        Usuario actualizado = usuarioRepository.save(usuario);

        return usuarioMapper.toResponseDTO(actualizado);
    }

    @Override
    public boolean existsById(Long id) {
        return usuarioRepository.existsById(id);
    }

    @Override
    public UsuarioResponseDTO asignarRol(Long usuarioId, Long rolId) {
        log.info("Asignando rol {} al usuario {}", rolId, usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        Rol nuevoRol = rolRepository.findById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + rolId));

        if (!nuevoRol.getActivo()) {
            throw new BusinessException("No se puede asignar un rol inactivo");
        }

        usuario.setRol(nuevoRol);
        Usuario actualizado = usuarioRepository.save(usuario);

        log.info("Rol asignado. Usuario: {}, Nuevo rol: {}", usuario.getEmail(), nuevoRol.getNombre());
        return usuarioMapper.toResponseDTO(actualizado);
    }
}