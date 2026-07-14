package com.pwc.routing.data;

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

    @Test
    void startupLoadsTheStartFileAndServesRoutesFromIt() throws IOException {
        Path startFile = tempDir.resolve("countries.json");
        Files.writeString(startFile, START_FILE_JSON);
        CountryDataService service = new CountryDataService(startFile, () -> {
            throw new IllegalStateException("fetcher must not be called at startup");
        });

        service.loadStartFile();

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
        Path startFile = tempDir.resolve("countries.json");
        Files.writeString(startFile, START_FILE_JSON);
        CountryDataService service = new CountryDataService(startFile, () -> fetchedJson);
        service.loadStartFile();

        service.refresh();

        assertThat(service.graph().findRoute("CZE", "ITA"))
                .isEqualTo(List.of("CZE", "ITA"));
        assertThat(startFile).hasContent(fetchedJson);
    }

    @Test
    void failedFetchKeepsLastGoodDataAndFile() throws IOException {
        Path startFile = tempDir.resolve("countries.json");
        Files.writeString(startFile, START_FILE_JSON);
        CountryDataService service = new CountryDataService(startFile, () -> {
            throw new IOException("network down");
        });
        service.loadStartFile();

        service.refresh();

        assertThat(service.graph().findRoute("CZE", "ITA"))
                .isEqualTo(List.of("CZE", "AUT", "ITA"));
        assertThat(startFile).hasContent(START_FILE_JSON);
    }

    @Test
    void malformedFetchResponseKeepsLastGoodDataAndFile() throws IOException {
        Path startFile = tempDir.resolve("countries.json");
        Files.writeString(startFile, START_FILE_JSON);
        CountryDataService service = new CountryDataService(startFile, () -> "<html>not json</html>");
        service.loadStartFile();

        service.refresh();

        assertThat(service.graph().findRoute("CZE", "ITA"))
                .isEqualTo(List.of("CZE", "AUT", "ITA"));
        assertThat(startFile).hasContent(START_FILE_JSON);
    }
}
