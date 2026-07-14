package com.pwc.routing.data;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.time.Duration;

@ConfigurationProperties(prefix = "countries")
public record CountriesProperties(Path file, Refresh refresh) {

    public record Refresh(String url, Duration interval) {
    }
}
