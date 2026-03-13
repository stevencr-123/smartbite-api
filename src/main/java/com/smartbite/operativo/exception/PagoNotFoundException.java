package com.smartbite.operativo.exception;

public class PagoNotFoundException extends ResourceNotFoundException {

    public PagoNotFoundException(String message) {
        super(message);
    }

    public PagoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
