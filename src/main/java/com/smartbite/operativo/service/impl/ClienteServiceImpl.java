package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.dto.cliente.ClienteRequestDTO;
import com.smartbite.operativo.dto.cliente.ClienteResponseDTO;
import com.smartbite.operativo.exception.BusinessException;
import com.smartbite.operativo.exception.ResourceNotFoundException;
import com.smartbite.operativo.mapper.ClienteMapper;
import com.smartbite.operativo.model.Cliente;
import com.smartbite.operativo.repository.ClienteRepository;
import com.smartbite.operativo.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public ClienteResponseDTO crearCliente(ClienteRequestDTO request) {
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new BusinessException("El nombre del cliente es obligatorio");
        }

        String numeroDocumento = request.getNumeroDocumento();
        if (numeroDocumento != null && !numeroDocumento.isBlank()
                && clienteRepository.findByNumeroDocumento(numeroDocumento).isPresent()) {
            throw new BusinessException("Ya existe un cliente con numeroDocumento: " + numeroDocumento);
        }

        Cliente cliente = clienteMapper.toEntity(request);
        if (cliente.getActivo() == null) {
            cliente.setActivo(Boolean.TRUE);
        }

        Cliente clienteGuardado = clienteRepository.save(cliente);
        return clienteMapper.toResponseDTO(clienteGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorId(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + clienteId));
        return clienteMapper.toResponseDTO(cliente);
    }
}

