package com.smartbite.operativo.model;

import com.smartbite.operativo.model.enums.EstadoMesa;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "mesas",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"numero", "sucursal_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMesa estado;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

    // Cross-module reference — Sucursal belongs to Administrativo
    @Column(name = "sucursal_id", nullable = false)
    private Long sucursalId;
}
