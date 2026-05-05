package com.smartbite.administrativo.service;

import com.smartbite.administrativo.dto.ProvedorRequestDTO;
import com.smartbite.administrativo.dto.ProvedorResponseDTO;
import java.util.List;

public interface ProvedorService {

    ProvedorResponseDTO crearProvedor(ProvedorRequestDTO requestDTO);

    ProvedorResponseDTO actualizarProvedor(Long id, ProvedorRequestDTO requestDTO);

    ProvedorResponseDTO obtenerProvedorPorId(Long id);

    List<ProvedorResponseDTO> obtenerTodosLosProvedores();

    List<ProvedorResponseDTO> obtenerProvedoresActivos();

    List<ProvedorResponseDTO> buscarProvedoresPorNombre(String nombre);

    void eliminarProvedor(Long id);

    ProvedorResponseDTO activarDesactivarProvedor(Long id, Boolean activo);
}