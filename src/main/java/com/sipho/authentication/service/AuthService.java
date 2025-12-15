package com.sipho.authentication.service;

import com.sipho.authentication.client.SupabaseAuthClient;
import com.sipho.authentication.dto.request.ResendOtpRequest;
import com.sipho.authentication.dto.request.SignupRequest;
import com.sipho.authentication.dto.request.VerifyOtpRequest;
import com.sipho.authentication.dto.response.AuthResponse;
import com.sipho.authentication.dto.supabase.SupabaseAuthResponse;
import com.sipho.authentication.dto.supabase.SupabaseOtpRequest;
import com.sipho.authentication.dto.supabase.SupabaseSignupRequest;
import com.sipho.authentication.dto.supabase.SupabaseVerifyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for authentication operations.
 * Handles user registration, OTP verification, and related business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final SupabaseAuthClient supabaseAuthClient;

    /**
     * Register a new user.
     * Creates user in Supabase and automatically sends OTP email.
     *
     * @param request Signup request with user details
     * @return Authentication response with user data
     */
    public AuthResponse registerUser(SignupRequest request) {
        log.info("Registering new user with email: {}", maskEmail(request.getEmail()));

        // Transform request to Supabase format
        Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("firstname", request.getFirstname());
        userMetadata.put("lastname", request.getLastname());

        SupabaseSignupRequest supabaseRequest = SupabaseSignupRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .data(userMetadata)
                .build();

        // Call Supabase to create user (OTP email sent automatically)
        SupabaseAuthResponse supabaseResponse = supabaseAuthClient.signup(supabaseRequest);

        log.info("User registered successfully. OTP sent to: {}", maskEmail(request.getEmail()));

        // Build response
        return AuthResponse.builder()
                .user(supabaseResponse.getUser())
                .session(null) // No session until OTP is verified
                .message("Registration successful. Please check your email for the verification code.")
                .build();
    }

    /**
     * Verify OTP code and authenticate user.
     * Returns session tokens on successful verification.
     *
     * @param request OTP verification request
     * @return Authentication response with user data and session tokens
     */
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        log.info("Verifying OTP for email: {}", maskEmail(request.getEmail()));

        // Transform request to Supabase format
        SupabaseVerifyRequest supabaseRequest = SupabaseVerifyRequest.builder()
                .email(request.getEmail())
                .token(request.getToken())
                .type(request.getType())
                .build();

        // Call Supabase to verify OTP
        SupabaseAuthResponse supabaseResponse = supabaseAuthClient.verifyOtp(supabaseRequest);

        log.info("OTP verified successfully for email: {}", maskEmail(request.getEmail()));

        // Build response with session tokens
        return AuthResponse.builder()
                .user(supabaseResponse.getUser())
                .session(supabaseResponse.getSession())
                .message("Email verified successfully. You are now logged in.")
                .build();
    }

    /**
     * Resend OTP code to user's email.
     * Used when user didn't receive the verification code.
     *
     * @param request Resend OTP request
     */
    public void resendOtp(ResendOtpRequest request) {
        log.info("Resending OTP to email: {}", maskEmail(request.getEmail()));

        // Transform request to Supabase format
        SupabaseOtpRequest supabaseRequest = SupabaseOtpRequest.builder()
                .email(request.getEmail())
                .createUser(false) // Don't create new user, just send OTP
                .build();

        // Call Supabase to send OTP
        supabaseAuthClient.sendOtp(supabaseRequest);

        log.info("OTP resent successfully to email: {}", maskEmail(request.getEmail()));
    }

    /**
     * Mask email for logging (security best practice).
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
