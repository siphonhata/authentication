package com.sipho.authentication.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuration for REST clients used in the application.
 * Creates a configured RestClient bean for Supabase API communication.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RestClientConfig {

    private final SupabaseProperties supabaseProperties;

    /**
     * Creates a RestClient configured for Supabase GoTrue API calls.
     *
     * @return Configured RestClient with base URL, headers, and timeouts
     */
    @Bean
    public RestClient supabaseRestClient() {
        String baseUrl = supabaseProperties.getAuth().getBaseUrl();

        // Validate URL format
        validateUrl(baseUrl);

        log.info("Initializing Supabase RestClient with base URL: {}", baseUrl);

        // Create HttpClient with timeout configuration
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(supabaseProperties.getHttp().getConnectTimeout()))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(supabaseProperties.getHttp().getReadTimeout()));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("apikey", supabaseProperties.getAnonKey())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    /**
     * Validate Supabase URL format.
     * Throws IllegalArgumentException if URL is invalid.
     */
    private void validateUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Supabase URL is not configured. Please set SUPABASE_URL environment variable."
            );
        }

        try {
            URL url = new URL(urlString);

            // Check protocol
            if (!"http".equalsIgnoreCase(url.getProtocol()) && !"https".equalsIgnoreCase(url.getProtocol())) {
                throw new IllegalArgumentException(
                        "Invalid Supabase URL protocol. Must be http or https. Current: " + urlString
                );
            }

            // Check host is present
            if (url.getHost() == null || url.getHost().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid Supabase URL. Missing host. Current: " + urlString
                );
            }

            // Warn if not using HTTPS
            if ("http".equalsIgnoreCase(url.getProtocol())) {
                log.warn("WARNING: Using HTTP instead of HTTPS for Supabase URL. This is insecure!");
            }

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(
                    "Invalid Supabase URL format: " + urlString + ". Error: " + e.getMessage(),
                    e
            );
        }
    }
}
