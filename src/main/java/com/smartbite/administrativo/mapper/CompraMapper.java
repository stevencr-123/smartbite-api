package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.CompraRequestDTO;
import com.smartbite.administrativo.dto.CompraResponseDTO;
import com.smartbite.administrativo.model.Compra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {DetalleCompraMapper.class})
public interface CompraMapper {

    CompraMapper INSTANCE = Mappers.getMapper(CompraMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "provedor", ignore = true)
    @Mapping(target = "sucursal", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    Compra toEntity(CompraRequestDTO requestDTO);

    @Mapping(target = "provedorId", source = "provedor.id")
    @Mapping(target = "provedorNombre", source = "provedor.nombre")
    @Mapping(target = "provedorNit", source = "provedor.nit")
    @Mapping(target = "sucursalId", source = "sucursal.id")
    @Mapping(target = "sucursalNombre", source = "sucursal.nombre")
    CompraResponseDTO toResponseDTO(Compra compra);
}