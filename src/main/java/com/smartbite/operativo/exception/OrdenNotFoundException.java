package com.smartbite.operativo.exception;

public class OrdenNotFoundException extends ResourceNotFoundException {

    public OrdenNotFoundException(String message) {
        super(message);
    }

    public OrdenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
