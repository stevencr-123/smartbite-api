package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.orden.OrdenResumenDTO;
import com.smartbite.operativo.dto.orden.OrdenResponseDTO;
import com.smartbite.operativo.model.Orden;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DetalleOrdenMapper.class, PagoMapper.class})
public interface OrdenMapper {

    @Mapping(source = "mesa.id", target = "mesaId")
    @Mapping(source = "mesa.numero", target = "numeroMesa")
    // nombreUsuario viene de Feign → se llena en service
    @Mapping(target = "nombreUsuario", ignore = true)
    OrdenResponseDTO toResponseDTO(Orden orden);

    @Mapping(source = "mesa.id", target = "mesaId")
    @Mapping(source = "mesa.numero", target = "numeroMesa")
    OrdenResumenDTO toResumenDTO(Orden orden);
}