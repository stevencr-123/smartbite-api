package com.smartbite.operativo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "metodos_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal comision = BigDecimal.ZERO;
}
