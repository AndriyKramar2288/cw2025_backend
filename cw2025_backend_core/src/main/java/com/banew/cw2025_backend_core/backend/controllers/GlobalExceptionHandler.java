package com.banew.cw2025_backend_core.backend.controllers;

import com.banew.cw2025_backend_common.dto.BasicResult;
import com.banew.cw2025_backend_common.dto.FieldExceptionResult;
import com.banew.cw2025_backend_core.backend.exceptions.MyBadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final static String VALIDATION_ERROR_MESSAGE = "Введені дані некоректні або відсутні!";

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MyBadRequestException.class)
    public BasicResult badRequestEx(MyBadRequestException ex) {
        return new BasicResult(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public FieldExceptionResult validationExc(MethodArgumentNotValidException ex) {
        return new FieldExceptionResult(
                ex.getBindingResult().getFieldErrors()
                        .stream()
                        .map(error -> new FieldExceptionResult.FieldException(
                                error.getField(),
                                error.getDefaultMessage()
                        ))
                        .toList(),
                VALIDATION_ERROR_MESSAGE,
                HttpStatus.BAD_REQUEST.value()
        );
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BasicResult badBody() {
        return new BasicResult(
                "Bad body format",
                HttpStatus.BAD_REQUEST.value()
        );
    }

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public BasicResult anyEx() {
        return new BasicResult(
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }
}