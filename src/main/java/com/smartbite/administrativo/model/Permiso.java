package com.smartbite.administrativo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "permisos",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_permiso_recurso_accion",
        columnNames = {"recurso", "accion"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 125)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String recurso;

    @Column(nullable = false, length = 20)
    private String accion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        this.nombre = construirNombre();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.nombre = construirNombre();
        this.fechaActualizacion = LocalDateTime.now();
    }

    private String construirNombre() {
        return recurso.toUpperCase() + ":" + accion.toUpperCase();
    }
}