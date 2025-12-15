package com.sipho.authentication.exception;

/**
 * Exception thrown when rate limit is exceeded.
 * HTTP Status: 429 TOO MANY REQUESTS
 */
public class RateLimitException extends SupabaseAuthException {

    public RateLimitException(String message) {
        super(message, 429, "RATE_LIMIT_EXCEEDED");
    }
}
