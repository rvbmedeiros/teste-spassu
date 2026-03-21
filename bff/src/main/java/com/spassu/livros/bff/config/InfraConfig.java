package com.spassu.livros.bff.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Configuration
public class InfraConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    @Qualifier("orchestrationClient")
    public WebClient orchestrationClient(
            WebClient.Builder builder,
            @Value("${spassu.orchestration.url}") String baseUrl) {
        return builder.baseUrl(normalizeLocalhost(baseUrl)).build();
    }

    @Bean
    @Qualifier("orchestrationInternalClient")
    public WebClient orchestrationInternalClient(
            WebClient.Builder builder,
            @Value("${spassu.orchestration.url}") String baseUrl) {
        return builder.baseUrl(normalizeLocalhost(baseUrl)).build();
    }

    @Bean
    @Qualifier("microserviceInternalClient")
    public WebClient microserviceInternalClient(
            WebClient.Builder builder,
            @Value("${spassu.microservice.url:http://localhost:8081}") String baseUrl) {
        return builder.baseUrl(normalizeLocalhost(baseUrl)).build();
    }

    private String normalizeLocalhost(String baseUrl) {
        try {
            URI uri = new URI(baseUrl);
            if (!"localhost".equalsIgnoreCase(uri.getHost())) {
                return baseUrl;
            }

            URI normalized = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    "127.0.0.1",
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment());

            log.info("Normalizing internal URL from {} to {} to avoid IPv6 localhost resolution issues", baseUrl, normalized);
            return normalized.toString();
        } catch (URISyntaxException ex) {
            log.warn("Could not normalize internal URL '{}'. Keeping original value.", baseUrl, ex);
            return baseUrl;
        }
    }
}
