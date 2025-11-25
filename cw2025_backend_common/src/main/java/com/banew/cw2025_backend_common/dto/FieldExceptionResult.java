package com.banew.cw2025_backend_common.dto;

import java.util.List;

public record FieldExceptionResult (
        List<FieldException> fieldErrors,
        String message,
        int code
) {

    public record FieldException (
            String field,
            String message
    ) { }
}
