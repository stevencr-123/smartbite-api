package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.MovimientoInventarioRequestDTO;
import com.smartbite.administrativo.dto.MovimientoInventarioResponseDTO;
import com.smartbite.administrativo.model.MovimientoInventario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MovimientoInventarioMapper {

    MovimientoInventarioMapper INSTANCE = Mappers.getMapper(MovimientoInventarioMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaMovimiento", ignore = true)
    @Mapping(target = "stockAntes", ignore = true)
    @Mapping(target = "stockDespues", ignore = true)
    @Mapping(target = "itemInventario", ignore = true)
    MovimientoInventario toEntity(MovimientoInventarioRequestDTO requestDTO);

    @Mapping(target = "itemInventarioId", source = "itemInventario.id")
    @Mapping(target = "itemInventarioNombre", source = "itemInventario.nombre")
    MovimientoInventarioResponseDTO toResponseDTO(MovimientoInventario movimiento);
}