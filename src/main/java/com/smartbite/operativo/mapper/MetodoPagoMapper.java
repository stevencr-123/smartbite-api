package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.metodopago.MetodoPagoResponseDTO;
import com.smartbite.operativo.model.MetodoPago;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MetodoPagoMapper {

    MetodoPagoResponseDTO toResponseDTO(MetodoPago metodoPago);
}
