package com.smartbite.operativo.model;

import com.smartbite.operativo.model.enums.TipoQR;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "codigos_qr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoQR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoQR tipo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    // --- Internal JPA relationship (Operativo module) ---

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id", unique = true)
    private Mesa mesa;

    // --- Cross-module reference (Administrativo) ---

    @Column(name = "producto_id")
    private Long productoId;
}
