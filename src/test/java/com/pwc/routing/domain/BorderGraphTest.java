package com.pwc.routing.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BorderGraphTest {

    private final BorderGraph graph = BorderGraph.of(List.of(
            country("CZE", List.of("AUT", "DEU", "POL", "SVK")),
            country("AUT", List.of("CZE", "DEU", "HUN", "ITA", "LIE", "SVK", "SVN", "CHE")),
            country("DEU", List.of("AUT", "BEL", "CZE", "DNK", "FRA", "LUX", "NLD", "POL", "CHE")),
            country("POL", List.of("BLR", "CZE", "DEU", "LTU", "RUS", "SVK", "UKR")),
            country("SVK", List.of("AUT", "CZE", "HUN", "POL", "UKR")),
            country("ITA", List.of("AUT", "FRA", "SMR", "SVN", "CHE", "VAT")),
            country("ISL", List.of())));

    /** Graph routing only looks at codes and borders; names are irrelevant here. */
    private static Country country(String cca3, List<String> borders) {
        return new Country(cca3, cca3, borders);
    }

    @Test
    void findsShortestLandRouteBetweenBorderingRegions() {
        assertThat(graph.findRoute("CZE", "ITA"))
                .isEqualTo(List.of("CZE", "AUT", "ITA"));
    }

    @Test
    void originEqualToDestinationIsTheTrivialZeroCrossingRoute() {
        assertThat(graph.findRoute("CZE", "CZE"))
                .isEqualTo(List.of("CZE"));
    }

    @Test
    void noLandRouteToAnIslandNation() {
        assertThatExceptionOfType(NoLandRouteException.class)
                .isThrownBy(() -> graph.findRoute("CZE", "ISL"))
                .withMessageContaining("CZE")
                .withMessageContaining("ISL");
    }

    @Test
    void tieBetweenEqualLengthRoutesBreaksAlphabetically() {
        BorderGraph diamond = BorderGraph.of(List.of(
                country("CZE", List.of("SVK", "AUT")),
                country("SVK", List.of("CZE", "HUN")),
                country("AUT", List.of("CZE", "HUN")),
                country("HUN", List.of("SVK", "AUT"))));

        assertThat(diamond.findRoute("CZE", "HUN"))
                .isEqualTo(List.of("CZE", "AUT", "HUN"));
    }

    @Test
    void unknownOriginIsRejected() {
        assertThatExceptionOfType(UnknownCountryCodeException.class)
                .isThrownBy(() -> graph.findRoute("XXX", "ITA"))
                .withMessageContaining("XXX");
    }

    @Test
    void unknownDestinationIsRejected() {
        assertThatExceptionOfType(UnknownCountryCodeException.class)
                .isThrownBy(() -> graph.findRoute("CZE", "XXX"))
                .withMessageContaining("XXX");
    }
}
