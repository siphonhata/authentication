package com.sipho.authentication.exception;

/**
 * Exception thrown when Supabase service is unavailable.
 * HTTP Status: 503 SERVICE_UNAVAILABLE
 */
public class ServiceUnavailableException extends SupabaseAuthException {

    public ServiceUnavailableException(String message) {
        super(message, 503, "SERVICE_UNAVAILABLE");
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, 503, "SERVICE_UNAVAILABLE", cause);
    }
}
