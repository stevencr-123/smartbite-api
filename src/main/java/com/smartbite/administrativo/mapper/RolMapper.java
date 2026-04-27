package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.RolRequestDTO;
import com.smartbite.administrativo.dto.RolResponseDTO;
import com.smartbite.administrativo.model.Permiso;
import com.smartbite.administrativo.model.Rol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper (componentModel = "spring")
public interface RolMapper {

    RolMapper INSTANCE = Mappers.getMapper(RolMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "permisos", ignore = true)
    Rol toEntity(RolRequestDTO requestDTO);

    RolResponseDTO toResponseDTO(Rol rol);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "permisos", ignore = true)
    void updateEntityFromRequest(RolRequestDTO requestDTO, @MappingTarget Rol entity);


}
