package com.smartbite.administrativo.service.impl;

import com.smartbite.administrativo.dto.RestauranteRequestDTO;
import com.smartbite.administrativo.dto.RestauranteResponseDTO;
import com.smartbite.administrativo.exception.BusinessException;
import com.smartbite.administrativo.exception.ResourceNotFoundException;
import com.smartbite.administrativo.mapper.RestauranteMapper;
import com.smartbite.administrativo.model.Restaurante;
import com.smartbite.administrativo.repository.RestauranteRepository;
import com.smartbite.administrativo.service.RestauranteService;
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
public class RestauranteServiceImpl implements RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final RestauranteMapper restauranteMapper;

    @Override
    public RestauranteResponseDTO crearRestaurante(RestauranteRequestDTO requestDTO){
        log.info("creando nuevo restaurante: ", requestDTO.getNombre());

        if (!requestDTO.getNif().matches("^\\d{1,10}-\\d{1}$")){
            throw new BusinessException("Formato de NIF invalido. debe ser:123456789-0");
        }


        if (restauranteRepository.existsByNif(requestDTO.getNif())){
            throw new BusinessException("Ya existe un restaurante con el NIF: "+ requestDTO.getNif());
        }


        if (restauranteRepository.findByEmail(requestDTO.getEmail()).isPresent()){
            throw new BusinessException("Ya existe un restaurante con el email: " + requestDTO.getEmail());
        }


        Restaurante restaurante = restauranteMapper.toEntity(requestDTO);
        Restaurante guardado = restauranteRepository.save(restaurante);

        log.info("Restaurante creado exitosamente con ID: ", guardado.getId());
        return restauranteMapper.toResponseDTO(guardado);
    }

    @Override
    public RestauranteResponseDTO actualizarRestaurante(Long id, RestauranteRequestDTO requestDTO) {
        log.info("Actualizando restaurante con ID: ",id);

        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante no encontrado con ID: " + id));

        if(restauranteRepository.existsByEmailAndIdNot( requestDTO.getEmail(), id)) {
            throw new BusinessException("Ya existe otro restaurante con este email: " + requestDTO.getEmail());
        }


        restauranteMapper.updateEntityFromRequest(requestDTO, restaurante);
        Restaurante actualizado = restauranteRepository.save(restaurante);

        log.info("Restaurante actualizado exitosamente");
        return restauranteMapper.toResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public RestauranteResponseDTO obtenerRestaurantePorId(Long id) {
        log.debug("Buscando restaurante por ID: ", id);

        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante no encontrado con ID: " + id));

        return restauranteMapper.toResponseDTO(restaurante);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestauranteResponseDTO> obtenerTodosLosRestaurantes(){
    log.debug("Obteniendo todos los restaurantes: ");

    return restauranteRepository.findAll().stream()
            .map(restauranteMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    public void eliminarRestaurante(Long id){
        log.info("Eliminando Restautante con ID: ", id);

        if (!restauranteRepository.existsById(id)) {
            throw new BusinessException("Restaurante no encontrado con ID: " + id);
        }

        restauranteRepository.deleteById(id);
        log.info("Restaurante eliminado exitosamente");
    }

    @Override
    public RestauranteResponseDTO activarDesactivarRestaurante(Long id, Boolean activo) {
        log.info("Cambiando estado del restaurante {} a activo={}", id, activo);

        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante no encontrado con ID: " + id));

        restaurante.setActivo(activo);
        Restaurante actualizado = restauranteRepository.save(restaurante);

        return restauranteMapper.toResponseDTO(actualizado);
    }
}
