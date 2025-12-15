package com.sipho.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic authentication response DTO.
 * Used for registration, login, and OTP verification responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UserData user;

    private SessionData session;

    private String message;
}
