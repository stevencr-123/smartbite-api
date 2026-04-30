package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.RestauranteResponseDTO;
import com.smartbite.administrativo.dto.RestauranteRequestDTO;
import java.util.List;

public interface RestauranteService {

    RestauranteResponseDTO crearRestaurante(RestauranteRequestDTO requestDTO);

    RestauranteResponseDTO actualizarRestaurante(Long id, RestauranteRequestDTO requestDTO);

    RestauranteResponseDTO obtenerRestaurantePorId(Long id);

    List<RestauranteResponseDTO> obtenerTodosLosRestaurantes();

    void eliminarRestaurante(Long id);

    RestauranteResponseDTO activarDesactivarRestaurante(Long id, Boolean activo);

}
