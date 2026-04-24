package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.qr.CodigoQRResponseDTO;
import com.smartbite.operativo.dto.qr.GenerarQRRequestDTO;
import com.smartbite.operativo.model.CodigoQR;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CodigoQRMapper {

    @Mapping(source = "mesa.id", target = "mesaId")
    CodigoQRResponseDTO toResponseDTO(CodigoQR codigoQR);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contenido", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaGeneracion", ignore = true)
    @Mapping(target = "mesa", ignore = true)
    CodigoQR toEntity(GenerarQRRequestDTO request);
}
