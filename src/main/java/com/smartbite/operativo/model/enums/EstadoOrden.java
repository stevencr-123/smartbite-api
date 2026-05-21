package com.smartbite.operativo.model.enums;

public enum EstadoOrden {

    /*
     * =====================================================
     * FLUJO OPERATIVO
     * =====================================================
     */

    PENDIENTE,

    EN_PREPARACION,

    LISTA,

    ENTREGADA,

    CANCELADA;

    public boolean esFinal() {

        return this == ENTREGADA
                || this == CANCELADA;
    }
}