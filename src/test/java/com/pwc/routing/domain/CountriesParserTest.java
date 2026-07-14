package com.pwc.routing.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CountriesParserTest {

    private final CountriesParser parser = new CountriesParser();

    @Test
    void parsesCca3AndBordersIgnoringOtherFields() {
        String json = """
                [
                  {"name": {"common": "Austria"}, "cca2": "AT", "cca3": "AUT",
                   "borders": ["CZE", "DEU", "HUN", "ITA", "LIE", "SVK", "SVN", "CHE"]},
                  {"name": {"common": "Iceland"}, "cca2": "IS", "cca3": "ISL",
                   "borders": []}
                ]
                """;

        List<Country> countries = parser.parse(json);

        assertThat(countries).containsExactly(
                new Country("AUT", List.of("CZE", "DEU", "HUN", "ITA", "LIE", "SVK", "SVN", "CHE")),
                new Country("ISL", List.of()));
    }
}
