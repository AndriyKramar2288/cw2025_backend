package com.banew.cw2025_backend_core.backend.exceptions;

public class MyBadRequestException extends RuntimeException {
    public MyBadRequestException(String message) {
        super(message);
    }
}
