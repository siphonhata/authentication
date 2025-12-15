package com.sipho.authentication.exception;

/**
 * Exception thrown when an OTP code has expired.
 * HTTP Status: 410 GONE
 */
public class OtpExpiredException extends SupabaseAuthException {

    public OtpExpiredException(String message) {
        super(message, 410, "OTP_EXPIRED");
    }
}
