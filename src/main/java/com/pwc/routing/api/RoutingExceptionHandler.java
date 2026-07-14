package com.pwc.routing.api;

import com.pwc.routing.domain.NoLandRouteException;
import com.pwc.routing.domain.UnknownCountryCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps domain failures to RFC 9457 Problem Details responses. Both "unknown
 * country code" and "no land route" are 400 per the API contract; the
 * {@code detail} field distinguishes them.
 */
@RestControllerAdvice
public class RoutingExceptionHandler {

    @ExceptionHandler({UnknownCountryCodeException.class, NoLandRouteException.class})
    public ProblemDetail badRoutingRequest(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
