package com.pwc.routing.data;

/**
 * Source of the raw countries.json document. The production implementation
 * fetches it over HTTP; tests substitute a stub.
 */
@FunctionalInterface
public interface CountriesFetcher {

    /**
     * @return the raw countries.json document
     * @throws Exception if the document cannot be retrieved
     */
    String fetch() throws Exception;
}
