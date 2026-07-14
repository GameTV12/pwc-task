package com.pwc.routing.domain;

public class UnknownCountryCodeException extends RuntimeException {

    public UnknownCountryCodeException(String cca3) {
        super("Unknown country code: " + cca3);
    }
}
