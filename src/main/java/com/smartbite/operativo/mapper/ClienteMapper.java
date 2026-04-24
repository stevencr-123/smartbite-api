package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.cliente.ClienteRequestDTO;
import com.smartbite.operativo.dto.cliente.ClienteResponseDTO;
import com.smartbite.operativo.model.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteResponseDTO toResponseDTO(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    Cliente toEntity(ClienteRequestDTO request);
}

