package com.sipho.authentication.dto.supabase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal DTO for Supabase OTP verification request.
 * Maps to the GoTrue /verify endpoint format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupabaseVerifyRequest {

    private String email;

    private String token;

    private String type;
}
