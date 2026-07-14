package com.pwc.routing.data;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(CountriesProperties.class)
public class CountriesConfig {

    @Bean
    public CountriesFetcher countriesFetcher(CountriesProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        RestClient client = RestClient.builder().requestFactory(requestFactory).build();
        return () -> client.get().uri(properties.refresh().url()).retrieve().body(String.class);
    }

    @Bean
    public CountryDataService countryDataService(CountriesProperties properties, CountriesFetcher fetcher) {
        CountryDataService service = new CountryDataService(properties.file(), fetcher);
        service.loadStartFile();
        return service;
    }
}
