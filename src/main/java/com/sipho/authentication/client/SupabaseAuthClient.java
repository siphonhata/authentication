package com.sipho.authentication.client;

import com.sipho.authentication.config.SupabaseProperties;
import com.sipho.authentication.dto.supabase.SupabaseAuthResponse;
import com.sipho.authentication.dto.supabase.SupabaseOtpRequest;
import com.sipho.authentication.dto.supabase.SupabaseSignupRequest;
import com.sipho.authentication.dto.supabase.SupabaseVerifyRequest;
import com.sipho.authentication.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Client for communicating with Supabase GoTrue authentication API.
 * Handles HTTP requests and response mapping.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SupabaseAuthClient {

    private final RestClient supabaseRestClient;
    private final SupabaseProperties supabaseProperties;

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
            SupabaseAuthResponse response = supabaseRestClient.post()
                    .uri("/signup")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, httpResp) -> {
                        int statusCode = httpResp.getStatusCode().value();
                        String body;
                        try {
                            body = new String(httpResp.getBody().readAllBytes());
                        } catch (Exception e) {
                            body = "Unable to read response body";
                        }

                        log.error("Supabase signup failed with status {}: {}", statusCode, body);

                        // Check for duplicate user - Supabase returns 422 or error message containing these keywords
                        if (statusCode == 422 ||
                            body.contains("already registered") ||
                            body.contains("User already registered") ||
                            body.contains("already been registered")) {
                            throw new UserAlreadyExistsException("User with this email already exists");
                        }
                        if (statusCode == 400) {
                            throw new SupabaseAuthException("Invalid request data. Please check your input.", 400, "BAD_REQUEST");
                        }
                        throw new SupabaseAuthException("Signup failed: " + body, statusCode, "SIGNUP_FAILED");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, httpResp) -> {
                        int statusCode = httpResp.getStatusCode().value();
                        log.error("Supabase server error during signup: {}", statusCode);
                        throw new ServiceUnavailableException("Supabase authentication service is temporarily unavailable. Please try again later.");
                    })
                    .body(SupabaseAuthResponse.class);

            // Log the response for debugging
            log.debug("Supabase signup response - User: {}, Session: {}",
                    response.getUser() != null ? response.getUser().getId() : "null",
                    response.getSession() != null ? "present" : "null");

            // Check if user already exists (Supabase returns success but with empty identities)
            if (response.getUser() != null &&
                response.getUser().getIdentities() != null &&
                response.getUser().getIdentities().isEmpty()) {
                log.warn("User signup returned empty identities - user likely already exists: {}", maskEmail(request.getEmail()));
                throw new UserAlreadyExistsException("User with this email already exists");
            }

            return response;
        } catch (SupabaseAuthException e) {
            throw e;
        } catch (ResourceAccessException e) {
            return handleNetworkError(e, "signup");
        } catch (Exception e) {
            log.error("Unexpected error during signup: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to connect to authentication service. Please check your configuration.", e);
        }
    }

    /**
     * Check if a user with the given email already exists.
     *
     * @param email Email address to check
     * @return true if user exists, false otherwise
     */
    public boolean checkUserExists(String email) {
        log.info("Checking if user exists for email: {}", maskEmail(email));

        try {
            // Use OTP endpoint with createUser=false to check existence
            // This will succeed (200) if user exists, fail (400) if user doesn't exist
            SupabaseOtpRequest request = SupabaseOtpRequest.builder()
                    .email(email)
                    .createUser(false)
                    .build();

            supabaseRestClient.post()
                    .uri("/otp")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                        int statusCode = response.getStatusCode().value();
                        String body;
                        try {
                            body = new String(response.getBody().readAllBytes());
                        } catch (Exception e) {
                            body = "Unable to read response body";
                        }

                        log.info("OTP check returned status {}, body: {}", statusCode, body);

                        // 429 means rate limit - user likely exists but we can't send OTP
                        if (statusCode == 429 && supabaseProperties.getRateLimit().isEnabled()) {
                            log.info("Rate limit reached, assuming user exists: {}", maskEmail(email));
                            return; // Don't throw, user exists
                        }

                        // Any other 4xx error means user doesn't exist (since createUser=false)
                        log.info("User does not exist (status {}): {}", statusCode, maskEmail(email));
                        throw new RuntimeException("USER_NOT_FOUND");
                    })
                    .toBodilessEntity();

            // If we get here without error (200 response), user exists
            log.info("User exists (OTP sent successfully): {}", maskEmail(email));
            return true;

        } catch (RuntimeException e) {
            // Check if this is our marker exception for user not found
            if (e.getMessage() != null && e.getMessage().contains("USER_NOT_FOUND")) {
                log.info("User existence check completed for {}: user does NOT exist", maskEmail(email));
                return false;
            }
            // Other runtime exceptions
            log.error("Unexpected error checking user existence for {}: {}", maskEmail(email), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            // Network or other errors
            log.error("Error checking user existence for {}: {}", maskEmail(email), e.getMessage(), e);
            return false;
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
                        String body;
                        try {
                            body = new String(response.getBody().readAllBytes());
                        } catch (Exception e) {
                            body = "Unable to read response body";
                        }

                        log.error("Supabase OTP request failed with status {}: {}", statusCode, body);

                        if (statusCode == 429 && supabaseProperties.getRateLimit().isEnabled()) {
                            String message = supabaseProperties.getRateLimit().getMessage()
                                    .replace("{waitTime}", String.valueOf(supabaseProperties.getRateLimit().getWaitTimeSeconds()));
                            throw new RateLimitException(message);
                        }
                        if (statusCode == 400) {
                            throw new SupabaseAuthException("Invalid email address.", 400, "BAD_REQUEST");
                        }
                        throw new SupabaseAuthException("OTP request failed: " + body, statusCode, "OTP_REQUEST_FAILED");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, response) -> {
                        int statusCode = response.getStatusCode().value();
                        log.error("Supabase server error during OTP request: {}", statusCode);
                        throw new ServiceUnavailableException("Authentication service is temporarily unavailable. Please try again later.");
                    })
                    .toBodilessEntity();

            log.info("OTP sent successfully to email: {}", maskEmail(request.getEmail()));
        } catch (SupabaseAuthException e) {
            throw e;
        } catch (ResourceAccessException e) {
            handleNetworkError(e, "send OTP");
        } catch (Exception e) {
            log.error("Unexpected error sending OTP: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to send OTP. Please check your network connection.", e);
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
                        String body;
                        try {
                            body = new String(response.getBody().readAllBytes());
                        } catch (Exception e) {
                            body = "Unable to read response body";
                        }

                        log.error("Supabase OTP verification failed with status {}: {}", statusCode, body);

                        if (statusCode == 401 || body.contains("invalid") || body.contains("Token has expired")) {
                            throw new InvalidOtpException("The OTP code provided is incorrect or has expired");
                        }
                        if (statusCode == 410 || body.contains("expired")) {
                            throw new OtpExpiredException("The OTP code has expired. Please request a new one.");
                        }
                        if (statusCode == 400) {
                            throw new SupabaseAuthException("Invalid verification request. Please check your input.", 400, "BAD_REQUEST");
                        }
                        throw new SupabaseAuthException("OTP verification failed: " + body, statusCode, "VERIFICATION_FAILED");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, response) -> {
                        int statusCode = response.getStatusCode().value();
                        log.error("Supabase server error during OTP verification: {}", statusCode);
                        throw new ServiceUnavailableException("Authentication service is temporarily unavailable. Please try again later.");
                    })
                    .body(SupabaseAuthResponse.class);
        } catch (SupabaseAuthException e) {
            throw e;
        } catch (ResourceAccessException e) {
            return handleNetworkError(e, "verify OTP");
        } catch (Exception e) {
            log.error("Unexpected error verifying OTP: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to verify OTP. Please check your network connection.", e);
        }
    }

    /**
     * Handle network-related errors with detailed messages.
     */
    private <T> T handleNetworkError(ResourceAccessException ex, String operation) {
        Throwable cause = ex.getCause();

        if (cause instanceof UnknownHostException) {
            log.error("Invalid Supabase URL or DNS resolution failed during {}: {}", operation, cause.getMessage());
            throw new ConfigurationException(
                    "Invalid Supabase URL. Please check your SUPABASE_URL configuration. Current error: Unable to resolve host.",
                    ex
            );
        }

        if (cause instanceof ConnectException) {
            log.error("Connection refused during {}: {}", operation, cause.getMessage());
            throw new ServiceUnavailableException(
                    "Unable to connect to Supabase. Please check if the URL is correct and the service is accessible.",
                    ex
            );
        }

        if (cause instanceof SocketTimeoutException) {
            log.error("Connection timeout during {}: {}", operation, cause.getMessage());
            throw new ServiceUnavailableException(
                    "Connection to Supabase timed out. The service may be slow or unreachable. Please try again.",
                    ex
            );
        }

        // Generic network error
        log.error("Network error during {}: {}", operation, ex.getMessage());
        throw new ServiceUnavailableException(
                "Network error occurred while connecting to authentication service. Please check your internet connection.",
                ex
        );
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
