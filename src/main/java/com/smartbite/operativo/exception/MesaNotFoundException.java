package com.smartbite.operativo.exception;

public class MesaNotFoundException extends ResourceNotFoundException {

    public MesaNotFoundException(String message) {
        super(message);
    }

    public MesaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
