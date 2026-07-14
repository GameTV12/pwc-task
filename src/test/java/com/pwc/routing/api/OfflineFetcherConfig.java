package com.pwc.routing.api;

import com.pwc.routing.data.CountriesFetcher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

/**
 * Stubs the HTTP fetcher to always fail, so an integration test boots exactly
 * as in production — startup data from the committed start file — but never
 * touches the network and never rewrites the start file.
 */
@TestConfiguration
public class OfflineFetcherConfig {

    @Bean
    @Primary
    CountriesFetcher failingFetcher() {
        return () -> {
            throw new IOException("offline in tests");
        };
    }
}
