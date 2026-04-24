package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.detalle.AgregarProductoRequestDTO;
import com.smartbite.operativo.dto.detalle.DetalleOrdenResponseDTO;
import com.smartbite.operativo.model.DetalleOrden;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DetalleOrdenMapper {

    @Mapping(target = "nombreProducto", ignore = true)
    DetalleOrdenResponseDTO toResponseDTO(DetalleOrden detalleOrden);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "precioUnitario", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "orden", ignore = true)
    DetalleOrden toEntity(AgregarProductoRequestDTO request);
}
