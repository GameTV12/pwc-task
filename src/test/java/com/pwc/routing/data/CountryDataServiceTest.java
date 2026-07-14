package com.pwc.routing.data;

import com.pwc.routing.domain.Country;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CountryDataServiceTest {

    private static final String START_FILE_JSON = """
            [
              {"cca3": "CZE", "borders": ["AUT"]},
              {"cca3": "AUT", "borders": ["CZE", "ITA"]},
              {"cca3": "ITA", "borders": ["AUT"]}
            ]
            """;

    @TempDir
    Path tempDir;

    private Path startFile;

    /** A service already loaded from a start file containing {@link #START_FILE_JSON}. */
    private CountryDataService loadedService(CountriesFetcher fetcher) throws IOException {
        startFile = tempDir.resolve("countries.json");
        Files.writeString(startFile, START_FILE_JSON);
        CountryDataService service = new CountryDataService(startFile, fetcher);
        service.loadStartFile();
        return service;
    }

    @Test
    void startupLoadsTheStartFileAndServesRoutesFromIt() throws IOException {
        CountryDataService service = loadedService(() -> {
            throw new IllegalStateException("fetcher must not be called at startup");
        });

        assertThat(service.graph().findRoute("CZE", "ITA"))
                .isEqualTo(List.of("CZE", "AUT", "ITA"));
    }

    @Test
    void successfulRefreshRewritesTheStartFileAndSwapsTheGraph() throws IOException {
        // Fetched data adds a direct CZE-ITA border, so the next answer changes.
        String fetchedJson = """
                [
                  {"cca3": "CZE", "borders": ["AUT", "ITA"]},
                  {"cca3": "AUT", "borders": ["CZE", "ITA"]},
                  {"cca3": "ITA", "borders": ["AUT", "CZE"]}
                ]
                """;
        CountryDataService service = loadedService(() -> fetchedJson);

        service.refresh();

        assertThat(service.graph().findRoute("CZE", "ITA"))
                .isEqualTo(List.of("CZE", "ITA"));
        assertThat(startFile).hasContent(fetchedJson);
    }

    @Test
    void failedFetchKeepsLastGoodDataAndFile() throws IOException {
        CountryDataService service = loadedService(() -> {
            throw new IOException("network down");
        });

        service.refresh();

        assertThat(service.graph().findRoute("CZE", "ITA"))
                .isEqualTo(List.of("CZE", "AUT", "ITA"));
        assertThat(startFile).hasContent(START_FILE_JSON);
    }

    @Test
    void countriesListReflectsTheCurrentDatasetAfterRefresh() throws IOException {
        String fetchedJson = """
                [
                  {"name": {"common": "Czechia"}, "cca3": "CZE", "borders": ["SVK"]},
                  {"name": {"common": "Slovakia"}, "cca3": "SVK", "borders": ["CZE"]}
                ]
                """;
        CountryDataService service = loadedService(() -> fetchedJson);

        assertThat(service.countries()).extracting(Country::cca3)
                .containsExactly("CZE", "AUT", "ITA");

        service.refresh();

        assertThat(service.countries()).extracting(Country::cca3)
                .containsExactly("CZE", "SVK");
        assertThat(service.countries()).extracting(Country::name)
                .containsExactly("Czechia", "Slovakia");
    }

    @Test
    void malformedFetchResponseKeepsLastGoodDataAndFile() throws IOException {
        CountryDataService service = loadedService(() -> "<html>not json</html>");

        service.refresh();

        assertThat(service.graph().findRoute("CZE", "ITA"))
                .isEqualTo(List.of("CZE", "AUT", "ITA"));
        assertThat(startFile).hasContent(START_FILE_JSON);
    }
}
