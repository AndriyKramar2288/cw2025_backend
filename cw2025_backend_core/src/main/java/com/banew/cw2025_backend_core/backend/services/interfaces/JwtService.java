package com.banew.cw2025_backend_core.backend.services.interfaces;

import com.banew.cw2025_backend_core.backend.entities.UserProfile;

public interface JwtService {
    String encodeJwt(UserProfile user);
}
