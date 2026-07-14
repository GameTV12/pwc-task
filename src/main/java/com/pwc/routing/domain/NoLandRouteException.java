package com.pwc.routing.domain;

public class NoLandRouteException extends RuntimeException {

    public NoLandRouteException(String origin, String destination) {
        super("No land route from " + origin + " to " + destination);
    }
}
