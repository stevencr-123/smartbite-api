package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.SucursalRequestDTO;
import com.smartbite.administrativo.dto.SucursalResponseDTO;
import com.smartbite.administrativo.model.Sucursal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SucursalMapper {

    SucursalMapper INSTANCE = Mappers.getMapper(SucursalMapper.class);

    // RequestDTO → Entity (ignorando la relación con Restaurante)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "restaurante", ignore = true)  // ← Se asigna manualmente en el Service
    Sucursal toEntity(SucursalRequestDTO requestDTO);

    // Entity → ResponseDTO
    @Mapping(target = "restauranteId", source = "restaurante.id")
    @Mapping(target = "restauranteNombre", source = "restaurante.nombre")
    SucursalResponseDTO toResponseDTO(Sucursal sucursal);
}