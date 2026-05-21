package com.smartbite.operativo.service.impl;

import com.smartbite.operativo.dto.cliente.ClienteResponseDTO;
import com.smartbite.operativo.dto.cliente.CrearClienteRequestDTO;
import com.smartbite.operativo.exception.BusinessException;
import com.smartbite.operativo.exception.ResourceNotFoundException;
import com.smartbite.operativo.mapper.ClienteMapper;
import com.smartbite.operativo.model.Cliente;
import com.smartbite.operativo.repository.ClienteRepository;
import com.smartbite.operativo.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl
        implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public ClienteResponseDTO crearCliente(
            CrearClienteRequestDTO request
    ) {

        if (request == null) {

            throw new BusinessException(
                    "Request inválido"
            );
        }

        /*
         * =====================================================
         * VALIDAR NOMBRE
         * =====================================================
         */
        if (request.getNombre() == null
                || request.getNombre().isBlank()) {

            throw new BusinessException(
                    "Nombre obligatorio"
            );
        }

        /*
         * =====================================================
         * VALIDAR DOCUMENTO
         * =====================================================
         */
        if (request.getNumeroDocumento() != null
                && request.getNumeroDocumento().length() > 30) {

            throw new BusinessException(
                    "Documento inválido"
            );
        }

        /*
         * =====================================================
         * VALIDAR DOCUMENTO ÚNICO
         * =====================================================
         */
        if (request.getNumeroDocumento() != null
                && clienteRepository.existsByNumeroDocumento(
                request.getNumeroDocumento()
        )) {

            throw new BusinessException(
                    "Ya existe un cliente con ese documento"
            );
        }

        Cliente cliente =
                clienteMapper.toEntity(
                        request
                );

        Cliente guardado =
                clienteRepository.save(
                        cliente
                );

        return clienteMapper.toResponseDTO(
                guardado
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerClientePorId(
            Long clienteId
    ) {

        Cliente cliente =
                clienteRepository.findById(
                        clienteId
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cliente no encontrado"
                        )
                );

        return clienteMapper.toResponseDTO(
                cliente
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarClientes() {

        return clienteRepository.findAll()
                .stream()
                .map(clienteMapper::toResponseDTO)
                .toList();
    }
}