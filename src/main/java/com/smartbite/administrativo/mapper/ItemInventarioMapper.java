package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.ItemInventarioRequestDTO;
import com.smartbite.administrativo.dto.ItemInventarioResponseDTO;
import com.smartbite.administrativo.model.ItemInventario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ItemInventarioMapper {

    ItemInventarioMapper INSTANCE = Mappers.getMapper(ItemInventarioMapper.class);

    // RequestDTO → Entity (ignorando relaciones)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "sucursal", ignore = true)  // ← Se asigna manualmente
    ItemInventario toEntity(ItemInventarioRequestDTO requestDTO);

    // Entity → ResponseDTO
    @Mapping(target = "sucursalId", source = "sucursal.id")
    @Mapping(target = "sucursalNombre", source = "sucursal.nombre")
    ItemInventarioResponseDTO toResponseDTO(ItemInventario itemInventario);
}