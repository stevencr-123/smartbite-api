package com.smartbite.administrativo.enums;

public enum TipoMovimientoInventario {
    ENTRADA("Entrada", "Ingreso de producto al inventario"),
    SALIDA("Salida", "Salida de producto del inventario"),
    AJUSTE("Ajuste", "Ajuste manual de inventario"),
    MERMA("Merma", "Pérdida de producto");

    private final String nombre;
    private final String descripcion;

    TipoMovimientoInventario(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}