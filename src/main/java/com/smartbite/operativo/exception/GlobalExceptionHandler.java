package com.smartbite.operativo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Component("operativoExceptionHandler")
public class GlobalExceptionHandler {

    /*
     * =========================================================
     * NOT FOUND
     * =========================================================
     */

    @ExceptionHandler({
            ResourceNotFoundException.class,
            OrdenNotFoundException.class,
            MesaNotFoundException.class
    })
    public ResponseEntity<Map<String, Object>> handleNotFound(
            RuntimeException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }

    /*
     * =========================================================
     * BAD REQUEST
     * =========================================================
     */

    @ExceptionHandler({
            InvalidStateException.class,
            EstadoOrdenInvalidoException.class,
            OrdenNoPagadaException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            RuntimeException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    /*
     * =========================================================
     * BUSINESS RULES
     * =========================================================
     */

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(
            BusinessException ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage(),
                request
        );
    }

    /*
     * =========================================================
     * VALIDATION
     * =========================================================
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, Object> errors =
                new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );

        body.put(
                "error",
                "Validation Error"
        );

        body.put(
                "messages",
                errors
        );

        body.put(
                "path",
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(body);
    }

    /*
     * =========================================================
     * FALLBACK
     * =========================================================
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception ex,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: "
                        + ex.getMessage(),
                request
        );
    }

    /*
     * =========================================================
     * HELPER
     * =========================================================
     */

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                status.value()
        );

        body.put(
                "error",
                status.getReasonPhrase()
        );

        body.put(
                "message",
                message
        );

        body.put(
                "path",
                request.getRequestURI()
        );

        return new ResponseEntity<>(
                body,
                status
        );
    }
}v