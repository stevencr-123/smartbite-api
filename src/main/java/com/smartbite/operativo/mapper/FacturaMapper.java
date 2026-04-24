package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.factura.FacturaResponseDTO;
import com.smartbite.operativo.model.Factura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FacturaMapper {

    @Mapping(source = "venta.id", target = "ventaId")
    @Mapping(source = "cliente.id", target = "clienteId")
    FacturaResponseDTO toResponseDTO(Factura factura);
}

