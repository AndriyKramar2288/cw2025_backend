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
        return BasicResult.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .build();
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public FieldExceptionResult validationExc(MethodArgumentNotValidException ex) {
        return FieldExceptionResult.builder()
                .fieldErrors(ex.getBindingResult().getFieldErrors()
                        .stream()
                        .map(error -> FieldExceptionResult.FieldException.builder()
                                .field(error.getField())
                                .message(error.getDefaultMessage())
                                .build())
                        .toList())
                .message(VALIDATION_ERROR_MESSAGE)
                .code(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BasicResult badBody() {
        return BasicResult.builder()
                .message("Bad body format")
                .code(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public BasicResult anyEx() {
        return BasicResult.builder()
                .message("Internal server error")
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
    }
}