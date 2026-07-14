package com.pwc.routing.api;

import com.pwc.routing.data.CountryDataService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
public class CountriesController {

    private final CountryDataService countryDataService;

    public CountriesController(CountryDataService countryDataService) {
        this.countryDataService = countryDataService;
    }

    /** All countries in the current in-memory dataset, sorted by common name. */
    @Operation(summary = "List all countries",
            description = "Common name and cca3 code of every country in the current in-memory "
                    + "dataset, sorted alphabetically by name.")
    @GetMapping("/countries")
    public List<CountryEntry> countries() {
        return countryDataService.countries().stream()
                .map(country -> new CountryEntry(country.name(), country.cca3()))
                .sorted(Comparator.comparing(CountryEntry::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public record CountryEntry(String name, String cca3) {
    }
}
