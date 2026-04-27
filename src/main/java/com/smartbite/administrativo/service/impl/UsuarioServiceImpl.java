package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.UsuarioRequestDTO;
import com.smartbite.administrativo.dto.UsuarioResponseDTO;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO requestDTO) {
        log.info("Creando usuario: {}", requestDTO.getEmail());

        // Validar email único
        if (usuarioRepository.existsByEmail(requestDTO.getEmail())) {
            throw new BusinessException("Ya existe un usuario con el email: " + requestDTO.getEmail());
        }

        // Validar que la sucursal existe
        Sucursal sucursal = sucursalRepository.findById(requestDTO.getSucursalId())
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + requestDTO.getSucursalId()));

        // Validar que el rol existe
        Rol rol = rolRepository.findById(requestDTO.getRolId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + requestDTO.getRolId()));

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(requestDTO.getNombre());
        usuario.setEmail(requestDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        usuario.setTelefono(requestDTO.getTelefono());
        usuario.setSucursal(sucursal);
        usuario.setRol(rol);

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado con ID: {}", guardado.getId());

        return convertToResponseDTO(guardado);
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

        // Actualizar campos
        usuario.setNombre(requestDTO.getNombre());
        usuario.setEmail(requestDTO.getEmail());
        if (requestDTO.getPassword() != null && !requestDTO.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        }
        usuario.setTelefono(requestDTO.getTelefono());

        // Actualizar sucursal si cambia
        if (!usuario.getSucursal().getId().equals(requestDTO.getSucursalId())) {
            Sucursal nuevaSucursal = sucursalRepository.findById(requestDTO.getSucursalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + requestDTO.getSucursalId()));
            usuario.setSucursal(nuevaSucursal);
        }

        // Actualizar rol si cambia
        if (!usuario.getRol().getId().equals(requestDTO.getRolId())) {
            Rol nuevoRol = rolRepository.findById(requestDTO.getRolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + requestDTO.getRolId()));
            usuario.setRol(nuevoRol);
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        log.info("Usuario actualizado con ID: {}", actualizado.getId());

        return convertToResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerUsuarioPorId(Long id) {
        log.debug("Buscando usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        return convertToResponseDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodosLosUsuarios() {
        log.debug("Obteniendo todos los usuarios");

        return usuarioRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerUsuariosPorSucursal(Long sucursalId) {
        log.debug("Obteniendo usuarios de la sucursal ID: {}", sucursalId);

        return usuarioRepository.findBySucursalId(sucursalId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerUsuariosPorRol(Long rolId) {
        log.debug("Obteniendo usuarios con rol ID: {}", rolId);

        return usuarioRepository.findByRolId(rolId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarUsuario(Long id) {
        log.info("Eliminando usuario con ID: {}", id);

        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }

        usuarioRepository.deleteById(id);
        log.info("Usuario eliminado con ID: {}", id);
    }

    @Override
    public UsuarioResponseDTO activarDesactivarUsuario(Long id, Boolean activo) {
        log.info("Cambiando estado del usuario {} a activo={}", id, activo);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(activo);
        Usuario actualizado = usuarioRepository.save(usuario);

        return convertToResponseDTO(actualizado);
    }


    private UsuarioResponseDTO convertToResponseDTO(Usuario usuario) {
        UsuarioResponseDTO responseDTO = new UsuarioResponseDTO();
        responseDTO.setId(usuario.getId());
        responseDTO.setNombre(usuario.getNombre());
        responseDTO.setEmail(usuario.getEmail());
        responseDTO.setTelefono(usuario.getTelefono());
        responseDTO.setActivo(usuario.getActivo());
        responseDTO.setFechaCreacion(usuario.getFechaCreacion());
        responseDTO.setFechaActualizacion(usuario.getFechaActualizacion());

        if (usuario.getSucursal() != null) {
            responseDTO.setSucursalId(usuario.getSucursal().getId());
            responseDTO.setSucursalNombre(usuario.getSucursal().getNombre());
        }

        if (usuario.getRol() != null) {
            responseDTO.setRolId(usuario.getRol().getId());
            responseDTO.setRolNombre(usuario.getRol().getNombre());
        }

        return responseDTO;
    }
}