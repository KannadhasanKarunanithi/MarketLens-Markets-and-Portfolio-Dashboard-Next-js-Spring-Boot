package com.marketlens.auth.dto;

import com.marketlens.user.UserSummary;

public record AuthResponse(String accessToken, String refreshToken, UserSummary user) {
}
