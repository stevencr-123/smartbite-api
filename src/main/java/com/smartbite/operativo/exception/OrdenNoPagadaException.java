package com.smartbite.operativo.exception;

public class OrdenNoPagadaException extends BusinessException {

    public OrdenNoPagadaException(String message) {
        super(message);
    }

    public OrdenNoPagadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
