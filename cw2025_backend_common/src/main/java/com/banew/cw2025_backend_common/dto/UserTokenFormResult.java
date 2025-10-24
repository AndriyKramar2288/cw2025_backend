package com.banew.cw2025_backend_common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserTokenFormResult {
    private String token;
    private String message;
    private int code;
}