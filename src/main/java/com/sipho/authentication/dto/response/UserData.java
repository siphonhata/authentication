package com.sipho.authentication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * User data response DTO.
 * Contains user information from Supabase.
 */
@Data
@Builder 
@NoArgsConstructor
@AllArgsConstructor
public class UserData {

    private String id;

    private String email;

    @JsonProperty("user_metadata")
    private Map<String, Object> userMetadata;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("confirmed_at")
    private String confirmedAt;

    @JsonProperty("email_confirmed_at")
    private String emailConfirmedAt;

    /**
     * List of user identities from authentication providers.
     * Empty list indicates user already exists (duplicate signup attempt).
     */
    private List<Map<String, Object>> identities;
}
