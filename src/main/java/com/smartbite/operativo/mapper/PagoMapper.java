package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.pago.CrearPagoRequestDTO;
import com.smartbite.operativo.dto.pago.PagoResponseDTO;
import com.smartbite.operativo.model.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(source = "orden.id", target = "ordenId")
    @Mapping(source = "metodoPago.id", target = "metodoPagoId")
    @Mapping(source = "metodoPago.nombre", target = "metodoPagoNombre")
    PagoResponseDTO toResponseDTO(Pago pago);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "orden", ignore = true)
    @Mapping(target = "metodoPago", ignore = true)
    Pago toEntity(CrearPagoRequestDTO request);
}