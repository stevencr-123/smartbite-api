package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.pago.CrearPagoRequestDTO;
import com.smartbite.operativo.dto.pago.PagoResponseDTO;
import com.smartbite.operativo.model.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    /*
     * =========================================================
     * ENTITY -> RESPONSE DTO
     * =========================================================
     */
    @Mapping(source = "orden.id", target = "ordenId")
    @Mapping(source = "metodoPago.id", target = "metodoPagoId")
    @Mapping(source = "metodoPago.nombre", target = "metodoPagoNombre")

    /*
     * =========================================================
     * NUEVOS CAMPOS FINANCIEROS
     * =========================================================
     */
    @Mapping(source = "proveedorPago", target = "proveedorPago")
    @Mapping(source = "sessionId", target = "sessionId")

    PagoResponseDTO toResponseDTO(Pago pago);

    /*
     * =========================================================
     * REQUEST DTO -> ENTITY
     * =========================================================
     */
    @Mapping(target = "id", ignore = true)

    @Mapping(target = "fechaPago", ignore = true)

    @Mapping(target = "estado", ignore = true)

    @Mapping(target = "orden", ignore = true)

    @Mapping(target = "metodoPago", ignore = true)

    /*
     * =========================================================
     * CAMPOS CONTROLADOS POR BACKEND
     * =========================================================
     */
    @Mapping(target = "proveedorPago", ignore = true)

    @Mapping(target = "sessionId", ignore = true)

    Pago toEntity(CrearPagoRequestDTO request);
}