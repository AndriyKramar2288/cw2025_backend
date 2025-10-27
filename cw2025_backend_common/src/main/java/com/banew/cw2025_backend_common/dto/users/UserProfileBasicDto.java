package com.banew.cw2025_backend_common.dto.users;

import lombok.Data;

@Data
public class UserProfileBasicDto {
    private String username;
    private String email;
    private String photoSrc;
}