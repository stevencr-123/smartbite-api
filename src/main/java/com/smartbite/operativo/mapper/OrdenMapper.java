package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.orden.OrdenResumenDTO;
import com.smartbite.operativo.dto.orden.OrdenResponseDTO;
import com.smartbite.operativo.model.Orden;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DetalleOrdenMapper.class, PagoMapper.class})
public interface OrdenMapper {

    @Mapping(source = "mesa.id", target = "mesaId")
    OrdenResponseDTO toResponseDTO(Orden orden);

    @Mapping(source = "mesa.id", target = "mesaId")
    OrdenResumenDTO toResumenDTO(Orden orden);
}
