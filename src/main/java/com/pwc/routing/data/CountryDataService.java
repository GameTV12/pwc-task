package com.pwc.routing.data;

import com.pwc.routing.domain.BorderGraph;
import com.pwc.routing.domain.CountriesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Owns the countries dataset lifecycle: loads the committed start file at
 * startup and exposes the current in-memory {@link BorderGraph}.
 */
public class CountryDataService {

    private static final Logger log = LoggerFactory.getLogger(CountryDataService.class);

    private final Path startFile;
    private final CountriesFetcher fetcher;
    private final CountriesParser parser = new CountriesParser();
    private final AtomicReference<BorderGraph> graph = new AtomicReference<>();

    public CountryDataService(Path startFile, CountriesFetcher fetcher) {
        this.startFile = startFile;
        this.fetcher = fetcher;
    }

    public void loadStartFile() {
        try {
            graph.set(BorderGraph.of(parser.parse(Files.readString(startFile))));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read countries start file " + startFile, e);
        }
    }

    /**
     * Re-fetches the dataset. On success the start file is rewritten and the
     * in-memory graph swapped; on any failure the last good data stays in
     * place and the next scheduled run retries.
     */
    public void refresh() {
        try {
            String json = fetcher.fetch();
            BorderGraph fresh = BorderGraph.of(parser.parse(json));
            Files.writeString(startFile, json);
            graph.set(fresh);
            log.info("Countries data refreshed, start file {} rewritten", startFile);
        } catch (Exception e) {
            log.warn("Countries refresh failed, keeping last good data: {}", e.toString());
        }
    }

    public BorderGraph graph() {
        return graph.get();
    }
}
