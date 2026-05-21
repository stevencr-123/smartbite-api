package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.cliente.ClienteResponseDTO;
import com.smartbite.operativo.dto.cliente.CrearClienteRequestDTO;

import java.util.List;

public interface ClienteService {

    ClienteResponseDTO crearCliente(
            CrearClienteRequestDTO request
    );

    ClienteResponseDTO obtenerClientePorId(
            Long clienteId
    );

    List<ClienteResponseDTO> listarClientes();
}