package com.sipho.authentication.controller;

import com.sipho.authentication.dto.request.ResendOtpRequest;
import com.sipho.authentication.dto.request.SignupRequest;
import com.sipho.authentication.dto.request.VerifyOtpRequest;
import com.sipho.authentication.dto.response.AuthResponse;
import com.sipho.authentication.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication endpoints.
 * Handles user registration, OTP verification, and related operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     * Automatically sends OTP email for verification.
     *
     * POST /api/v1/auth/register
     *
     * @param request Signup request with user details
     * @return Authentication response with user data
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody SignupRequest request) {
        log.info("Received registration request for email: {}",
                request.getEmail().substring(0, 1) + "***");

        AuthResponse response = authService.registerUser(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Verify OTP code.
     * Returns session tokens on successful verification.
     *
     * POST /api/v1/auth/verify-otp
     *
     * @param request OTP verification request
     * @return Authentication response with session tokens
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("Received OTP verification request for email: {}",
                request.getEmail().substring(0, 1) + "***");

        AuthResponse response = authService.verifyOtp(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Resend OTP code to user's email.
     * Rate limited by Supabase (1 request per 60 seconds).
     *
     * POST /api/v1/auth/resend-otp
     *
     * @param request Resend OTP request
     * @return Success message
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        log.info("Received OTP resend request for email: {}",
                request.getEmail().substring(0, 1) + "***");

        authService.resendOtp(request);

        return ResponseEntity.ok(Map.of(
                "message", "OTP code has been sent to your email"
        ));
    }

    /**
     * Health check endpoint.
     * Used to verify the API is running.
     *
     * GET /api/v1/auth/health
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Authentication API"
        ));
    }
}
