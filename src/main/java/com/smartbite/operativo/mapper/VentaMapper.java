package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.venta.VentaResponseDTO;
import com.smartbite.operativo.model.Venta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VentaMapper {

    @Mapping(source = "orden.id", target = "ordenId")
    VentaResponseDTO toResponseDTO(Venta venta);
}

