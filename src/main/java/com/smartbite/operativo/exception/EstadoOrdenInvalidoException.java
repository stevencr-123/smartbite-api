package com.smartbite.operativo.exception;

public class EstadoOrdenInvalidoException extends InvalidStateException {

    public EstadoOrdenInvalidoException(String message) {
        super(message);
    }

    public EstadoOrdenInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
