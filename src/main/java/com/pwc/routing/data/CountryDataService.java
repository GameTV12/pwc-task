package com.pwc.routing.data;

import com.pwc.routing.domain.BorderGraph;
import com.pwc.routing.domain.CountriesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
     * Re-fetches the dataset. On success the in-memory graph is swapped and
     * the start file rewritten; on any failure the last good data stays in
     * place and the next scheduled run retries.
     */
    public void refresh() {
        String json;
        BorderGraph fresh;
        try {
            json = fetcher.fetch();
            fresh = BorderGraph.of(parser.parse(json));
        } catch (Exception e) {
            log.warn("Countries refresh failed, keeping last good data: {}", e.toString());
            return;
        }
        graph.set(fresh);
        rewriteStartFile(json);
    }

    /**
     * Write-then-rename so a crash mid-write can never leave a truncated
     * start file behind — the next boot must always find a loadable snapshot.
     * A failed rewrite only makes the file one refresh staler than memory.
     */
    private void rewriteStartFile(String json) {
        try {
            Path temp = Files.createTempFile(startFile.toAbsolutePath().getParent(), "countries", ".json.tmp");
            Files.writeString(temp, json);
            Files.move(temp, startFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            log.info("Countries data refreshed, start file {} rewritten", startFile);
        } catch (IOException e) {
            log.warn("Countries data refreshed in memory, but rewriting start file {} failed: {}",
                    startFile, e.toString());
        }
    }

    public BorderGraph graph() {
        return graph.get();
    }
}
