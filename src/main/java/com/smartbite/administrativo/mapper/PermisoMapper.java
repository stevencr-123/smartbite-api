package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.PermisoRequestDTO;
import com.smartbite.administrativo.dto.PermisoResponseDTO;
import com.smartbite.administrativo.model.Permiso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PermisoMapper {

    PermisoMapper INSTANCE = Mappers.getMapper(PermisoMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Permiso toEntity(PermisoRequestDTO requestDTO);

    PermisoResponseDTO toResponseDTO(Permiso permiso);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateEntityFromRequest(PermisoRequestDTO requestDTO, @MappingTarget Permiso entity);
}