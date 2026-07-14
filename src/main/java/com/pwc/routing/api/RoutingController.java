package com.pwc.routing.api;

import com.pwc.routing.data.CountryDataService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
public class RoutingController {

    private final CountryDataService countryDataService;

    public RoutingController(CountryDataService countryDataService) {
        this.countryDataService = countryDataService;
    }

    @Operation(summary = "Shortest land route between two countries",
            description = "Origin and destination are cca3 codes (case-insensitive). Returns the "
                    + "route as an inclusive list of cca3 codes; 400 if a code is unknown or no "
                    + "land route exists.")
    @GetMapping("/routing/{origin}/{destination}")
    public RouteResponse route(@PathVariable String origin, @PathVariable String destination) {
        List<String> route = countryDataService.graph()
                .findRoute(canonical(origin), canonical(destination));
        return new RouteResponse(route);
    }

    private static String canonical(String cca3) {
        return cca3.toUpperCase(Locale.ROOT);
    }

    public record RouteResponse(List<String> route) {
    }
}
