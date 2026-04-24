package com.smartbite.operativo.model.enums;

public enum EstadoOrden {
    PENDIENTE,
    EN_PREPARACION,
    LISTA,
    ENTREGADA,
    CANCELADA,
    PAGADA;

    public boolean esFinal() {
        return this == PAGADA || this == CANCELADA;
    }
}
