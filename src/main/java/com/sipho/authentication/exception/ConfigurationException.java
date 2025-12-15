package com.sipho.authentication.exception;

/**
 * Exception thrown when there's a configuration error.
 * HTTP Status: 500 INTERNAL_SERVER_ERROR (but with helpful message)
 */
public class ConfigurationException extends SupabaseAuthException {

    public ConfigurationException(String message) {
        super(message, 500, "CONFIGURATION_ERROR");
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, 500, "CONFIGURATION_ERROR", cause);
    }
}
