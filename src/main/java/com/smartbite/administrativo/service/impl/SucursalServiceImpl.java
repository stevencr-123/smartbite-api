package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.SucursalRequestDTO;
import com.smartbite.administrativo.dto.SucursalResponseDTO;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.model.Restaurante;
import com.smartbite.administrativo.model.Sucursal;
import com.smartbite.administrativo.repository.RestauranteRepository;
import com.smartbite.administrativo.repository.SucursalRepository;
import com.smartbite.administrativo.service.SucursalService;
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
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepository;
    private final RestauranteRepository restauranteRepository;

    @Override
    public SucursalResponseDTO crearSucursal(SucursalRequestDTO requestDTO) {
        log.info("Creando sucursal: {} para restaurante ID: {}",
                requestDTO.getNombre(), requestDTO.getRestauranteId());

        Restaurante restaurante = restauranteRepository.findById(requestDTO.getRestauranteId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante no encontrado con ID: " + requestDTO.getRestauranteId()));

        if (sucursalRepository.existsByNombreAndRestauranteId(requestDTO.getNombre(), requestDTO.getRestauranteId())) {
            throw new BusinessException("Ya existe una sucursal con el nombre '" + requestDTO.getNombre() + "' en este restaurante");
        }

        if (requestDTO.getEmail() != null && sucursalRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new BusinessException("Ya existe una sucursal con el email: " + requestDTO.getEmail());
        }

        // Conversión manual de RequestDTO a Entity
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre(requestDTO.getNombre());
        sucursal.setDireccion(requestDTO.getDireccion());
        sucursal.setTelefono(requestDTO.getTelefono());
        sucursal.setTelefonoSecundario(requestDTO.getTelefonoSecundario());
        sucursal.setEmail(requestDTO.getEmail());
        sucursal.setHorarioAtencion(requestDTO.getHorarioAtencion());
        sucursal.setRestaurante(restaurante);

        Sucursal guardado = sucursalRepository.save(sucursal);

        log.info("Sucursal creada con ID: {}", guardado.getId());

        // Conversión manual de Entity a ResponseDTO
        return convertToResponseDTO(guardado);
    }

    @Override
    public SucursalResponseDTO actualizarSucursal(Long id, SucursalRequestDTO requestDTO) {
        log.info("Actualizando sucursal con ID: {}", id);

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + id));

        if (requestDTO.getEmail() != null && sucursalRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
            throw new BusinessException("Ya existe otra sucursal con el email: " + requestDTO.getEmail());
        }

        // Actualizar campos manualmente
        sucursal.setNombre(requestDTO.getNombre());
        sucursal.setDireccion(requestDTO.getDireccion());
        sucursal.setTelefono(requestDTO.getTelefono());
        sucursal.setTelefonoSecundario(requestDTO.getTelefonoSecundario());
        sucursal.setEmail(requestDTO.getEmail());
        sucursal.setHorarioAtencion(requestDTO.getHorarioAtencion());

        // Si cambia el restaurante
        if (!sucursal.getRestaurante().getId().equals(requestDTO.getRestauranteId())) {
            Restaurante nuevoRestaurante = restauranteRepository.findById(requestDTO.getRestauranteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurante no encontrado con ID: " + requestDTO.getRestauranteId()));
            sucursal.setRestaurante(nuevoRestaurante);
        }

        Sucursal actualizado = sucursalRepository.save(sucursal);
        log.info("Sucursal actualizada con ID: {}", actualizado.getId());

        return convertToResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public SucursalResponseDTO obtenerSucursalPorId(Long id) {
        log.debug("Buscando sucursal con ID: {}", id);

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + id));

        return convertToResponseDTO(sucursal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalResponseDTO> obtenerSucursalesPorRestaurante(Long restauranteId) {
        log.debug("Obteniendo sucursales del restaurante ID: {}", restauranteId);

        return sucursalRepository.findByRestauranteId(restauranteId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalResponseDTO> obtenerTodasLasSucursales() {
        log.debug("Obteniendo todas las sucursales");

        return sucursalRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarSucursal(Long id) {
        log.info("Eliminando sucursal con ID: {}", id);

        if (!sucursalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sucursal no encontrada con ID: " + id);
        }

        sucursalRepository.deleteById(id);
        log.info("Sucursal eliminada con ID: {}", id);
    }

    @Override
    public SucursalResponseDTO activarDesactivarSucursal(Long id, Boolean activo) {
        log.info("Cambiando estado de sucursal {} a activo={}", id, activo);

        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + id));

        sucursal.setActivo(activo);
        Sucursal actualizado = sucursalRepository.save(sucursal);

        return convertToResponseDTO(actualizado);
    }

    // Método privado para convertir Entity a ResponseDTO
    private SucursalResponseDTO convertToResponseDTO(Sucursal sucursal) {
        SucursalResponseDTO responseDTO = new SucursalResponseDTO();
        responseDTO.setId(sucursal.getId());
        responseDTO.setNombre(sucursal.getNombre());
        responseDTO.setDireccion(sucursal.getDireccion());
        responseDTO.setTelefono(sucursal.getTelefono());
        responseDTO.setTelefonoSecundario(sucursal.getTelefonoSecundario());
        responseDTO.setEmail(sucursal.getEmail());
        responseDTO.setHorarioAtencion(sucursal.getHorarioAtencion());
        responseDTO.setActivo(sucursal.getActivo());
        responseDTO.setFechaCreacion(sucursal.getFechaCreacion());
        responseDTO.setFechaActualizacion(sucursal.getFechaActualizacion());

        if (sucursal.getRestaurante() != null) {
            responseDTO.setRestauranteId(sucursal.getRestaurante().getId());
            responseDTO.setRestauranteNombre(sucursal.getRestaurante().getNombre());
        }

        return responseDTO;
    }
}