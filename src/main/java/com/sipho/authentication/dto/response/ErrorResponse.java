package com.sipho.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized error response DTO.
 * Used by GlobalExceptionHandler for consistent error formatting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int statusCode;

    private String error;

    private String message;

    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();

    private String path;
}
