package com.smartbite.operativo.controller;

import com.smartbite.operativo.dto.cliente.ClienteResponseDTO;
import com.smartbite.operativo.dto.cliente.CrearClienteRequestDTO;
import com.smartbite.operativo.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponseDTO crearCliente(
            @Valid @RequestBody
            CrearClienteRequestDTO request
    ) {

        return clienteService.crearCliente(
                request
        );
    }

    @GetMapping("/{id}")
    public ClienteResponseDTO obtenerClientePorId(
            @PathVariable Long id
    ) {

        return clienteService.obtenerClientePorId(
                id
        );
    }

    @GetMapping
    public List<ClienteResponseDTO> listarClientes() {

        return clienteService.listarClientes();
    }
}