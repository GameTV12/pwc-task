package com.pwc.routing.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UncheckedIOException;
import java.io.IOException;
import java.util.List;

/**
 * Parses the mledoze/countries dataset (a JSON array of country objects),
 * keeping only the fields the routing domain needs.
 */
public class CountriesParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Country> parse(String json) {
        try {
            List<RawCountry> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream()
                    .map(c -> new Country(c.cca3(), c.borders() == null ? List.of() : c.borders()))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Malformed countries JSON", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RawCountry(String cca3, List<String> borders) {
    }
}
