package com.pwc.routing.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BorderGraphTest {

    private final BorderGraph graph = BorderGraph.of(List.of(
            new Country("CZE", List.of("AUT", "DEU", "POL", "SVK")),
            new Country("AUT", List.of("CZE", "DEU", "HUN", "ITA", "LIE", "SVK", "SVN", "CHE")),
            new Country("DEU", List.of("AUT", "BEL", "CZE", "DNK", "FRA", "LUX", "NLD", "POL", "CHE")),
            new Country("POL", List.of("BLR", "CZE", "DEU", "LTU", "RUS", "SVK", "UKR")),
            new Country("SVK", List.of("AUT", "CZE", "HUN", "POL", "UKR")),
            new Country("ITA", List.of("AUT", "FRA", "SMR", "SVN", "CHE", "VAT")),
            new Country("ISL", List.of())));

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
                new Country("CZE", List.of("SVK", "AUT")),
                new Country("SVK", List.of("CZE", "HUN")),
                new Country("AUT", List.of("CZE", "HUN")),
                new Country("HUN", List.of("SVK", "AUT"))));

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
