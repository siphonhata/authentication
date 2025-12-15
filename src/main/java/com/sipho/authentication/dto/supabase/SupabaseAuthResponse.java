package com.sipho.authentication.dto.supabase;

import com.sipho.authentication.dto.response.SessionData;
import com.sipho.authentication.dto.response.UserData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal DTO for Supabase authentication responses.
 * Maps the response from Supabase GoTrue API endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupabaseAuthResponse {

    private UserData user;

    private SessionData session;
}
