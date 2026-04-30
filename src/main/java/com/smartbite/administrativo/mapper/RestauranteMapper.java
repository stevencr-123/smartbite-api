package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.RestauranteRequestDTO;
import com.smartbite.administrativo.dto.RestauranteResponseDTO;
import com.smartbite.administrativo.model.Restaurante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RestauranteMapper {

    RestauranteMapper INSTANCE = Mappers.getMapper(RestauranteMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    Restaurante toEntity(RestauranteRequestDTO requestDTO);

    RestauranteResponseDTO toResponseDTO(Restaurante restaurante);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "sucursales", ignore = true)
    void updateEntityFromRequest(RestauranteRequestDTO requestDTO, @MappingTarget Restaurante entity);
}