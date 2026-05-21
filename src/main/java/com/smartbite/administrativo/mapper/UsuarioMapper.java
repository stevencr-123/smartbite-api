package com.smartbite.administrativo.mapper;

import com.smartbite.administrativo.dto.UsuarioRequestDTO;
import com.smartbite.administrativo.dto.UsuarioResponseDTO;
import com.smartbite.administrativo.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);

    // RequestDTO → Entity (ignorando relaciones)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "password", ignore = true)  // ← Se encripta en el Service
    @Mapping(target = "sucursal", ignore = true)  // ← Se asigna manualmente
    @Mapping(target = "rol", ignore = true)       // ← Se asigna manualmente
    Usuario toEntity(UsuarioRequestDTO requestDTO);

    // Entity → ResponseDTO
    @Mapping(target = "sucursalId", source = "sucursal.id")
    @Mapping(target = "sucursalNombre", source = "sucursal.nombre")
    @Mapping(target = "rolId", source = "rol.id")
    @Mapping(target = "rolNombre", source = "rol.nombre")
    UsuarioResponseDTO toResponseDTO(Usuario usuario);
}