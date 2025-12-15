package com.sipho.authentication.exception;

import lombok.Getter;

/**
 * Base exception for Supabase authentication errors.
 * All Supabase-specific exceptions should extend this class.
 */
@Getter
public class SupabaseAuthException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    public SupabaseAuthException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public SupabaseAuthException(String message, int statusCode, String errorCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
}
