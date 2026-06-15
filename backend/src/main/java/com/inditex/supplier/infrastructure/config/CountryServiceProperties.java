package com.inditex.supplier.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

/**
 * Typed configuration for the external country service.
 * Bound to the {@code country-service} prefix in {@code application.yml}.
 */
@ConfigurationProperties(prefix = "country-service")
public record CountryServiceProperties(@NonNull String baseUrl) {}
