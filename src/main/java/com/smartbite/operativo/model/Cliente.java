package com.smartbite.operativo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo_documento", length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", unique = true, length = 30)
    private String numeroDocumento;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}

