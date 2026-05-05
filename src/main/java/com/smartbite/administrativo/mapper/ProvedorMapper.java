package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.ProvedorRequestDTO;
import com.smartbite.administrativo.dto.ProvedorResponseDTO;
import com.smartbite.administrativo.model.Provedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProvedorMapper {

    ProvedorMapper INSTANCE = Mappers.getMapper(ProvedorMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Provedor toEntity(ProvedorRequestDTO requestDTO);

    ProvedorResponseDTO toResponseDTO(Provedor proveedor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateEntityFromRequest(ProvedorRequestDTO requestDTO, @MappingTarget Provedor entity);
}