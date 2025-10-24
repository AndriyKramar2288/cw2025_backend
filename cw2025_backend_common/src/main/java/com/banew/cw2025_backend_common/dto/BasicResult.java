package com.banew.cw2025_backend_common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BasicResult {
    private String message;
    private int code;
}
