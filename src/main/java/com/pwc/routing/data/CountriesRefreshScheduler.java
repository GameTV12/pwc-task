package com.pwc.routing.data;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Triggers a dataset refresh immediately after startup (default initial
 * delay of a fixed-delay task) and then once per configured interval.
 */
@Component
public class CountriesRefreshScheduler {

    private final CountryDataService countryDataService;

    public CountriesRefreshScheduler(CountryDataService countryDataService) {
        this.countryDataService = countryDataService;
    }

    @Scheduled(fixedDelayString = "${countries.refresh.interval}")
    public void refresh() {
        countryDataService.refresh();
    }
}
