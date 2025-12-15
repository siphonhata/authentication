package com.sipho.authentication.exception;

/**
 * Exception thrown when an invalid OTP code is provided.
 * HTTP Status: 401 UNAUTHORIZED
 */
public class InvalidOtpException extends SupabaseAuthException {

    public InvalidOtpException(String message) {
        super(message, 401, "INVALID_OTP");
    }
}
