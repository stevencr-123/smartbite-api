package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.DetalleCompraRequestDTO;
import com.smartbite.administrativo.dto.DetalleCompraResponseDTO;
import com.smartbite.administrativo.model.DetalleCompra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DetalleCompraMapper {

    DetalleCompraMapper INSTANCE = Mappers.getMapper(DetalleCompraMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "cantidadRecibida", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "compra", ignore = true)
    @Mapping(target = "itemInventario", ignore = true)
    DetalleCompra toEntity(DetalleCompraRequestDTO requestDTO);

    @Mapping(target = "itemInventarioId", source = "itemInventario.id")
    @Mapping(target = "itemInventarioNombre", source = "itemInventario.nombre")
    @Mapping(target = "itemInventarioUnidadMedida", source = "itemInventario.unidadMedida")
    DetalleCompraResponseDTO toResponseDTO(DetalleCompra detalle);
}