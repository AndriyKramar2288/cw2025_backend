package com.banew.cw2025_backend_common.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FieldExceptionResult {
    List<FieldException> fieldErrors;
    private String message;
    private int code;

    @Builder
    @Getter
    public static class FieldException {
        private String field;
        private String message;
    }
}
