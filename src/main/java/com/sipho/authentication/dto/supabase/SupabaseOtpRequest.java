package com.sipho.authentication.dto.supabase;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal DTO for Supabase OTP request.
 * Maps to the GoTrue /otp endpoint format for sending OTP codes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupabaseOtpRequest {

    private String email;

    /**
     * Whether to create a new user if they don't exist.
     * Set to false for resending OTP to existing users.
     */
    @JsonProperty("create_user")
    @Builder.Default
    private boolean createUser = false;
}
