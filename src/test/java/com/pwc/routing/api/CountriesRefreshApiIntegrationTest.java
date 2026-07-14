package com.pwc.routing.api;

import com.pwc.routing.data.CountriesFetcher;
import com.pwc.routing.data.CountryDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code GET /countries} must reflect a swapped dataset: after a successful
 * refresh the endpoint serves the freshly fetched countries, not the ones the
 * app booted with. The start file is a temp copy of the committed one so the
 * refresh in this test rewrites that copy, never {@code data/countries.json}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CountriesRefreshApiIntegrationTest {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void startFileIsATempCopy(DynamicPropertyRegistry registry) throws IOException {
        Path startFile = tempDir.resolve("countries.json");
        Files.copy(Path.of("data/countries.json"), startFile);
        registry.add("countries.file", startFile::toString);
    }

    /** Fails like {@link OfflineFetcherConfig} until the test provides a response. */
    @TestConfiguration
    static class SwitchableFetcher {

        static final AtomicReference<String> response = new AtomicReference<>();

        @Bean
        @Primary
        CountriesFetcher switchableFetcher() {
            return () -> {
                String json = response.get();
                if (json == null) {
                    throw new IOException("offline in tests");
                }
                return json;
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CountryDataService countryDataService;

    @Test
    void countriesReflectsTheSwappedDatasetAfterARefresh() throws Exception {
        mockMvc.perform(get("/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(200)));

        SwitchableFetcher.response.set("""
                [
                  {"name": {"common": "Czechia"}, "cca3": "CZE", "borders": ["SVK"]},
                  {"name": {"common": "Slovakia"}, "cca3": "SVK", "borders": ["CZE"]}
                ]
                """);
        countryDataService.refresh();

        mockMvc.perform(get("/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Czechia"))
                .andExpect(jsonPath("$[0].cca3").value("CZE"))
                .andExpect(jsonPath("$[1].name").value("Slovakia"))
                .andExpect(jsonPath("$[1].cca3").value("SVK"));
    }
}
