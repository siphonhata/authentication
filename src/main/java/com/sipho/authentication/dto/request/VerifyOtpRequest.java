package com.sipho.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for OTP verification.
 * Contains email and the OTP token sent to the user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "OTP token is required")
    @Size(min = 6, max = 6, message = "OTP token must be exactly 6 digits")
    @Pattern(regexp = "\\d{6}", message = "OTP token must contain only digits")
    private String token;

    @Builder.Default
    private String type = "signup";
}
