package com.sipho.authentication.client;

import com.sipho.authentication.dto.supabase.SupabaseAuthResponse;
import com.sipho.authentication.dto.supabase.SupabaseOtpRequest;
import com.sipho.authentication.dto.supabase.SupabaseSignupRequest;
import com.sipho.authentication.dto.supabase.SupabaseVerifyRequest;
import com.sipho.authentication.exception.InvalidOtpException;
import com.sipho.authentication.exception.OtpExpiredException;
import com.sipho.authentication.exception.RateLimitException;
import com.sipho.authentication.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Client for communicating with Supabase GoTrue authentication API.
 * Handles HTTP requests and response mapping.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SupabaseAuthClient {

    private final RestClient supabaseRestClient;

    /**
     * Register a new user with Supabase.
     * Automatically sends an OTP email for verification.
     *
     * @param request Signup request with email, password, and user metadata
     * @return Authentication response with user data
     * @throws UserAlreadyExistsException if user with email already exists
     */
    public SupabaseAuthResponse signup(SupabaseSignupRequest request) {
        log.debug("Calling Supabase signup endpoint for email: {}", maskEmail(request.getEmail()));

        try {
            return supabaseRestClient.post()
                    .uri("/signup")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        int statusCode = response.getStatusCode().value();
                        String body = new String(response.getBody().readAllBytes());

                        log.error("Supabase signup failed with status {}: {}", statusCode, body);

                        if (statusCode == 422 || body.contains("already registered")) {
                            throw new UserAlreadyExistsException("User with this email already exists");
                        }
                        throw new RuntimeException("Signup failed: " + body);
                    })
                    .body(SupabaseAuthResponse.class);
        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during signup: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }

    /**
     * Send an OTP code to a user's email.
     * Used for resending verification codes.
     *
     * @param request OTP request with email
     * @throws RateLimitException if rate limit is exceeded
     */
    public void sendOtp(SupabaseOtpRequest request) {
        log.debug("Calling Supabase OTP endpoint for email: {}", maskEmail(request.getEmail()));

        try {
            supabaseRestClient.post()
                    .uri("/otp")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        int statusCode = response.getStatusCode().value();
                        String body = new String(response.getBody().readAllBytes());

                        log.error("Supabase OTP request failed with status {}: {}", statusCode, body);

                        if (statusCode == 429) {
                            throw new RateLimitException("Too many OTP requests. Please wait before trying again.");
                        }
                        throw new RuntimeException("OTP request failed: " + body);
                    })
                    .toBodilessEntity();

            log.info("OTP sent successfully to email: {}", maskEmail(request.getEmail()));
        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error sending OTP: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP: " + e.getMessage(), e);
        }
    }

    /**
     * Verify an OTP code and authenticate the user.
     *
     * @param request Verification request with email, token, and type
     * @return Authentication response with user data and session tokens
     * @throws InvalidOtpException if OTP is incorrect
     * @throws OtpExpiredException if OTP has expired
     */
    public SupabaseAuthResponse verifyOtp(SupabaseVerifyRequest request) {
        log.debug("Calling Supabase verify endpoint for email: {}", maskEmail(request.getEmail()));

        try {
            return supabaseRestClient.post()
                    .uri("/verify")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        int statusCode = response.getStatusCode().value();
                        String body = new String(response.getBody().readAllBytes());

                        log.error("Supabase OTP verification failed with status {}: {}", statusCode, body);

                        if (statusCode == 401 || body.contains("invalid") || body.contains("Token has expired")) {
                            throw new InvalidOtpException("The OTP code provided is incorrect or has expired");
                        }
                        if (statusCode == 410 || body.contains("expired")) {
                            throw new OtpExpiredException("The OTP code has expired. Please request a new one.");
                        }
                        throw new RuntimeException("OTP verification failed: " + body);
                    })
                    .body(SupabaseAuthResponse.class);
        } catch (InvalidOtpException | OtpExpiredException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying OTP: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to verify OTP: " + e.getMessage(), e);
        }
    }

    /**
     * Mask email for logging (security best practice).
     * Example: john.doe@example.com -> j***e@example.com
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email.charAt(0) + "***@" + email.substring(atIndex + 1);
        }
        return email.charAt(0) + "***" + email.charAt(atIndex - 1) + email.substring(atIndex);
    }
}
