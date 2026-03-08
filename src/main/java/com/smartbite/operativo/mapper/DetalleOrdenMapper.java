package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.detalle.DetalleOrdenResponseDTO;
import com.smartbite.operativo.model.DetalleOrden;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DetalleOrdenMapper {

    @Mapping(target = "nombreProducto", ignore = true)
    DetalleOrdenResponseDTO toResponseDTO(DetalleOrden detalleOrden);
}
