package org.pdzsoftware.moviereservationsystem.exception.custom;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {
    protected ApiException(String message) {
        super(message);
    }

    public abstract HttpStatus getStatus();
}
