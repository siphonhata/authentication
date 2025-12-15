package com.sipho.authentication.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

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
        log.info("Initializing Supabase RestClient with base URL: {}",
                supabaseProperties.getAuth().getBaseUrl());

        // Create HttpClient with timeout configuration
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(supabaseProperties.getHttp().getConnectTimeout()))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(supabaseProperties.getHttp().getReadTimeout()));

        return RestClient.builder()
                .baseUrl(supabaseProperties.getAuth().getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("apikey", supabaseProperties.getAnonKey())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
