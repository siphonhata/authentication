package com.sipho.authentication.dto.supabase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Internal DTO for Supabase signup API request.
 * Maps to the GoTrue /signup endpoint format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupabaseSignupRequest { 

    private String email;

    private String password;

    /**
     * User metadata (firstname, lastname, etc.)
     * This is stored in the user_metadata field in Supabase.
     */
    private Map<String, Object> data;
}
