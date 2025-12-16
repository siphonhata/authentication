package com.sipho.authentication.exception;

/**
 * Exception thrown when attempting to register a user that already exists.
 * HTTP Status: 409 CONFLICT
 */
public class UserAlreadyExistsException extends SupabaseAuthException {

    public UserAlreadyExistsException(String message) {
        super(message, 409, "USER_ALREADY_EXISTS");
    }
}
 