package com.sipho.authentication.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Supabase integration.
 * Binds properties prefixed with "supabase" from application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "supabase")
@Data
@Validated
public class SupabaseProperties {

    @NotBlank(message = "Supabase URL is required")
    private String url;

    @NotBlank(message = "Supabase anon key is required")
    private String anonKey;

    private String serviceRoleKey;

    private AuthConfig auth = new AuthConfig();
    private HttpConfig http = new HttpConfig();
    private OtpConfig otp = new OtpConfig();
    private RateLimitConfig rateLimit = new RateLimitConfig();

    @Data
    public static class AuthConfig {
        private String baseUrl;
    }

    @Data
    public static class HttpConfig {
        private int connectTimeout = 5000;
        private int readTimeout = 10000;
    }

    @Data
    public static class OtpConfig {
        private int expiryMinutes = 60;
        private String emailTemplateType = "signup";
    }

    @Data
    public static class RateLimitConfig {
        private boolean enabled = true;
        private int waitTimeSeconds = 60;
        private String message = "Too many OTP requests. Please wait {waitTime} seconds before trying again.";
    }
}
