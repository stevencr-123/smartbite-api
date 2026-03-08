package com.smartbite.operativo.mapper;

import com.smartbite.operativo.dto.mesa.MesaResponseDTO;
import com.smartbite.operativo.model.Mesa;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MesaMapper {

    MesaResponseDTO toResponseDTO(Mesa mesa);
}

