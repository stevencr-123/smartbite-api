package com.smartbite.operativo.service;

import com.smartbite.operativo.dto.cliente.ClienteRequestDTO;
import com.smartbite.operativo.dto.cliente.ClienteResponseDTO;

public interface ClienteService {

    ClienteResponseDTO crearCliente(ClienteRequestDTO request);

    ClienteResponseDTO obtenerPorId(Long clienteId);
}

