package com.pwc.routing.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pwc.routing.data.CountriesFetcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots the application from the committed start file with the HTTP fetcher
 * stubbed to fail, exactly like {@link RoutingApiIntegrationTest} — offline,
 * start file never rewritten.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CountriesApiIntegrationTest {

    @TestConfiguration
    static class OfflineFetcher {

        @Bean
        @Primary
        CountriesFetcher failingFetcher() {
            return () -> {
                throw new IOException("offline in tests");
            };
        }
    }

    /** The documented response shape; deserialization fails on any extra field. */
    record CountryEntry(String name, String cca3) {
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void countriesReturnsAllCountriesAsNameAndCca3SortedByCommonName() throws Exception {
        String body = mockMvc.perform(get("/countries"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        List<CountryEntry> countries = objectMapper.readValue(body, new TypeReference<>() {});

        assertThat(countries).contains(new CountryEntry("Czechia", "CZE"));
        assertThat(countries).hasSizeGreaterThan(200);
        assertThat(countries).extracting(CountryEntry::name)
                .isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
    }
}
